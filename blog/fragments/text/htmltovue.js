module.exports = {
    convert: function($, f) {
        f.bindPath($)
        f.mapRichField($.find('div'), 'text')
    }
}