#import <UIKit/UIKit.h>
#import "UIImage+Scale.h"
#import "ImageResize.h"

@implementation ImageResize

-(void)resize:(CDVInvokedUrlCommand *)command {

    [self.commandDelegate runInBackground:^{
        NSDictionary* options = [command.arguments objectAtIndex:0];
        NSString* source = [options objectForKey:@"source"];
        if ([source hasPrefix:@"file:"]) {
            source = [[ NSURL URLWithString:source] path];
        }

        UIImage* image = [UIImage imageWithContentsOfFile:source];
        CGFloat desiredWidth = [[options objectForKey:@"width"] floatValue];
        CGFloat desiredHeight = [[options objectForKey:@"height"] floatValue];
        if (image.size.width <= desiredWidth && image.size.height <= desiredHeight) {
            NSNumber* originalWidth = [[NSNumber alloc] initWithFloat:image.size.width];
            NSNumber* originalHeight = [[NSNumber alloc] initWithFloat:image.size.height];
            NSDictionary* result = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:source, originalWidth, originalHeight, nil] forKeys:[NSArray arrayWithObjects: @"filePath", @"width", @"height", nil]];
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            return;
        }

        CGSize factors = [self calculateFactors:options originalWidth:image.size.width originalHeight: image.size.height];
        UIImage* resizedImage = [image scaleToSize:CGSizeMake(image.size.width * factors.width, image.size.height * factors.height)];
        NSData* resizedImageData = UIImageJPEGRepresentation(resizedImage, [[options objectForKey:@"quality"] floatValue]);

        NSArray* paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
        NSMutableString* filePath = [NSMutableString stringWithString: [paths objectAtIndex:0]];
        [filePath appendString:@"/"];
        [filePath appendFormat:@"%f.resize.jpg", [[NSDate date] timeIntervalSince1970]];

        NSError *error = nil;
        CDVPluginResult* pluginResult = nil;

        bool written = [resizedImageData writeToFile:filePath options:NSDataWritingAtomic error:&error];
        if (written) {
            NSNumber* resizedWidth = [[NSNumber alloc] initWithFloat:resizedImage.size.width];
            NSNumber* resizedHeight = [[NSNumber alloc] initWithFloat:resizedImage.size.height];
            NSDictionary* result = [NSDictionary dictionaryWithObjects:[NSArray arrayWithObjects:filePath, resizedWidth, resizedHeight, nil] forKeys:[NSArray arrayWithObjects: @"filePath", @"width", @"height", nil]];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

-(CGSize)calculateFactors: (NSDictionary *)options originalWidth:(CGFloat) width originalHeight: (CGFloat) height {
    CGFloat widthFactor;
    CGFloat heightFactor;

    NSString* type = [options objectForKey:@"type"];
    CGFloat desiredWidth = [[options objectForKey:@"width"] floatValue];
    CGFloat desiredHeight = [[options objectForKey:@"height"] floatValue];

    if ([type isEqualToString:@"minPixelResize"]) {
        widthFactor = desiredWidth / width;
        heightFactor = desiredHeight / height;
        if (widthFactor > heightFactor && widthFactor <= 1.0) {
            heightFactor = widthFactor;
        } else if (heightFactor <= 1.0) {
            widthFactor = heightFactor;
        } else {
            widthFactor = 1.0;
            heightFactor = 1.0;
        }
    } else if ([type isEqualToString:@"maxPixelResize"]) {
        widthFactor = desiredWidth / width;
        heightFactor = desiredHeight / height;
        if (widthFactor == 0.0) {
            widthFactor = heightFactor;
        } else if (heightFactor == 0.0) {
            heightFactor = widthFactor;
        } else if (widthFactor > heightFactor) {
            widthFactor = heightFactor; // scale to fit height
        } else {
            heightFactor = widthFactor; // scale to fit width
        }
    } else {
        widthFactor = 1.0;
        heightFactor = 1.0;
    }

    CGSize factors;
    factors.width = widthFactor;
    factors.height = heightFactor;

    return factors;
}

@end