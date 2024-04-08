/* jshint -W121 */
//add ability to do
//var somestring = 'boom';
//somestring.contains('oo'); which will return true;
if (!('contains' in String.prototype)) {
    String.prototype.contains = function (str, startIndex) {
        return ''.indexOf.call(this, str, startIndex) !== -1;
    };
}

if (!String.prototype.startsWith) {
    String.prototype.startsWith = function (searchString, position) {
        position = position || 0;
        return this.lastIndexOf(searchString, position) === position;
    };
}

if (!Number.prototype.timesBy100) {
    Number.prototype.timesBy100 = function () {
        return this.valueOf() * 100;
    };
}

if (!Number.prototype.divideBy100) {
    Number.prototype.divideBy100 = function () {
        return this.valueOf() / 100;
    };
}