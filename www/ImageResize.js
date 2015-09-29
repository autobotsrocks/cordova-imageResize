var ImageResize = function() {
};

ImageResize.prototype.resize = function(options, success, fail) {
  options.quality = options.quality || 70;
  options.type = options.type || 'maxPixelResize';
  return cordova.exec(success, fail, 'ImageResize', 'resize', [options]);
};

module.exports = new ImageResize();