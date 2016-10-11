/*
 * Implementation for service discovery. It sends a message on the broadcast
 * address/port and listens to the responses. The service type to be looked up
 * is provided by the user.
 */


#import "serviceDiscovery.h"

NSMutableArray *serviceArr;

@implementation serviceDiscovery

/*
 * Does a service discovery for the given service type. Returns an array of
 * all the services discovered.
 */
- (void)getNetworkServices: (CDVInvokedUrlCommand*)command {

    NSString* serviceType = [command.arguments objectAtIndex:0];
    [self.commandDelegate runInBackground:^{

    CDVPluginResult* pluginResult = nil;
    if (serviceType == nil)
    {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"service not provided"];
    }
    else
    {

            serviceArr = [[NSMutableArray alloc] init];

            // Open a socket
            int sd = socket(PF_INET, SOCK_DGRAM, IPPROTO_UDP);
            if (sd <= 0) {
                NSLog(@"Error: Could not open socket");
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"TX socket creation failed"];
            }
            else {
                // Set socket options
                int broadcastEnable = 1;
                int ret = setsockopt(sd, SOL_SOCKET, SO_BROADCAST, &broadcastEnable, sizeof(broadcastEnable));
                if (ret) {
                    NSLog(@"Error: setsockopt failed to enable broadcast mode");
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"TX socket setsockopt failed"];
                    close(sd);
                }
                else {

                    // Configure the broadcast IP and port
                    struct sockaddr_in broadcastAddr;
                    memset(&broadcastAddr, 0, sizeof broadcastAddr);
                    broadcastAddr.sin_family = AF_INET;
                    inet_pton(AF_INET, "239.255.255.250", &broadcastAddr.sin_addr);
                    broadcastAddr.sin_port = htons(1900);

                    // Send the broadcast request for the given service type
                    NSString *request = [[@"M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nMAN: \"ssdp:discover\"\r\nST: " stringByAppendingString:serviceType] stringByAppendingString:@"\"\r\nMX: 2\r\n\r\n"];
                    char *requestStr = [request UTF8String];

                    ret = sendto(sd, requestStr, strlen(requestStr), 0, (struct sockaddr*)&broadcastAddr, sizeof broadcastAddr);
                    if (ret < 0) {
                        NSLog(@"Error: Could not send broadcast");
                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"sendto failed"];
                        close(sd);
                    }
                    else {

                        NSLog(@"ret:%d", ret);
                        NSLog(@"Bcast msg sent");


                        NSLog(@"recv: On to listening");

                        // set timeout to 2 seconds.
                        struct timeval timeV;
                        timeV.tv_sec = 2;
                        timeV.tv_usec = 0;

                        if (setsockopt(sd, SOL_SOCKET, SO_RCVTIMEO, &timeV, sizeof(timeV)) == -1) {
                            NSLog(@"Error: listenForPackets - setsockopt failed");
                            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"RX socket setsockopt failed"];
                            close(sd);
                        }
                        else {
                            NSLog(@"recv: socketopt set");

                            // receive
                            struct sockaddr_in receiveSockaddr;
                            socklen_t receiveSockaddrLen = sizeof(receiveSockaddr);
        
                            size_t bufSize = 9216;
                            void *buf = malloc(bufSize);
                                        NSLog(@"recv: listening now: %d", sd);


                            // Keep listening till the socket timeout event occurs
                            while (true)
                            {
                                ssize_t result = recvfrom(sd, buf, bufSize, 0,
                                                          (struct sockaddr *)&receiveSockaddr,
                                                          (socklen_t *)&receiveSockaddrLen);
//                                NSLog(@"got sthing:%ld", result);

                                if (result < 0)
                                {
                                    NSLog(@"timeup");
                                    break;
                                }

                                NSData *data = nil;
                                data = [NSData dataWithBytesNoCopy:buf length:result freeWhenDone:NO];

                                NSString *msg = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];

                                [self processResponse:msg];
                            }

                            free(buf);
                            close(sd);

                            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:serviceArr];
                        }
                    }
                }
            }
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}


/*
 * Processes the response received from a UPnP device.
 * Converts the string response to a NSMutableDictionary.
 */
- (void)processResponse:(NSString *)message
{
//    NSLog(@"%@", message);
    
    NSArray *msgLines = [message componentsSeparatedByString:@"\r"];

//    NSLog(@"total lines:%lu", [msgLines count]);

    NSMutableDictionary *data = [[NSMutableDictionary alloc] init];

    int i = 0;
    for (i = 0; i < [msgLines count]; i++)
    {
     //   NSLog(@"working on:%@", msgLines[i]);
        NSRange range = [msgLines[i] rangeOfString:@":"];

        if(range.length == 1){
            NSRange p1range = NSMakeRange(0, range.location);
            NSString *part1 = [msgLines[i] substringWithRange:p1range];
            part1 = [part1 stringByTrimmingCharactersInSet:
                       [NSCharacterSet whitespaceAndNewlineCharacterSet]];
  //          NSLog(@"%@", part1);
            NSRange p2range = NSMakeRange(range.location + 1 , [msgLines[i] length] - range.location - 1);
            NSString *part2 = [msgLines[i] substringWithRange:p2range];
            part2 = [part2 stringByTrimmingCharactersInSet:
                     [NSCharacterSet whitespaceAndNewlineCharacterSet]];
  //          NSLog(@"%@", part2);

            data[part1] = part2;
        }
    }
    [serviceArr addObject: data];

}

@end

