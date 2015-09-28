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
    quality: 65,
    width: 80,
    height: 80
  },
  function(filePath) {
    alert(filePath);
  },
  function(error) {
    alert(error);
  }
);
```

# License

MIT
