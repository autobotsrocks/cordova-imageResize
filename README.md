# cordova-imageResize
A cordova plugin that provides the functionality to resize image on both android and ios.

# Installing the plugin

```shell
cordova plugin add https://github.com/autobotsrocks/cordova-imageResize
```

# Usage

```javascript
window.autobots.imageResize.resize(
  {
    source: '/storage/emulated/0/Pictures/hello.png',
    quality: 65, // Default 70
    type: 'maxPixelResize' | 'minPixelResize', //Default maxPixelResize
    width: 80,
    height: 80
  },
  function(response) {
    alert(response.filePath);
    alert(response.width);
    alert(response.height);
  },
  function(error) {
    alert(error);
  }
);
```

# Dependencies

Android: [glide](https://github.com/bumptech/glide)


# License

MIT
