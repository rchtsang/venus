function vgetBaseLog(x, y) {
    return Math.log(y) / Math.log(x);
}
function vdecimalToHexString(number)
{
    if (number < 0)
    {
        number = 0xFFFFFFFF + number + 1;
    }

    return number.toString(16).toUpperCase().substring(0,8);
}
function vnumToBase(curNumBase, n, length, base, signextend) {
    var amount = Math.pow(2, length);
    length = vgetBaseLog(curNumBase, amount);
    var num = 0;
    if (signextend) {
        num = parseInt(parseInt(n, base).toString(10), 10);
        num = parseInt(vdecimalToHexString(num), 16).toString(curNumBase);
    } else {
        num = parseInt(n, base).toString(curNumBase);
    }
    if (length - num.length > 0) {
        num = "0".repeat(length - num.length) + num;
    }
    snum = "";
    if (curNumBase == 2) {
        for (var i = 0; i < length; i++) {
            if (i % 4 == 0 && i != 0) {
                snum += " ";
            }
            snum += num[i];
        }
    } else {
        snum = num;
    }
    return snum;
}
function downloadtrace(id, filename, custom_name) {
    var cusN = prompt("Please enter a name for the file. Leave blank for default.");
    if (cusN != "") {
        if (cusN == null) {
            return;
        }
        if (cusN.split('.').pop() != filename.split('.').pop()) {
            cusN = cusN + "." + filename.split('.').pop();
        }
        filename = cusN;
    }
    var text = document.getElementById(id).value;
    var element = document.createElement('a');
    element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
    element.setAttribute('download', filename);

    element.style.display = 'none';
    document.body.appendChild(element);

    element.click();

    document.body.removeChild(element);
}
function CopyToClipboard(containerid) {
    var copyText = document.getElementById(containerid);

    /* Select the text field */
    copyText.select();

    /* Copy the text inside the text field */
    document.execCommand("Copy");
    return
}
function validateBase(e) {
    if (e.value == "") {
        return;
    }
    if (e.value < 1) {
        e.value = 2;
    }
    if (e.value > 32) {
        e.value = 32;
    }
}
function toggleThis(e) {
    if (e.value == "true") {
        e.classList.remove("is-primary");
        e.value = "false";
    } else {
        e.classList.add("is-primary");
        e.value = "true";
    }
}

var LocalStorageManager = class LocalStorageManager{
    constructor(name) {
        this.name = name;
        this.vls = {};
        this.getObj();
    }
    getObj() {
        var v = localStorage.getItem(this.name);
        if (v == null) {
            this.setup();
            return;
        }
        var jsn = JSON.parse(v);
        if (typeof jsn === 'object' && jsn[this.name] !== undefined) {
            this.vls = jsn;
        } else {
            this.setup();
        }
    }
    writeObj() {
        localStorage.setItem(this.name, JSON.stringify(this.vls));
    }
    setup() {
        this.vls[this.name] = 'false';
        this.writeObj();
    }
    get(key) {
        var v = this.vls[key];
        if (v === undefined) {
            v = 'undefined';
        }
        return v
    }
    set(key, value) {
        if (this.vls[key] === value) {
            return
        }
        this.vls[key] = value;
        this.writeObj();
    }
    remove(key) {
        delete this.vls[key];
        this.writeObj();
    }
    reset() {
        this.vls = {}
        this.setup()
    }
}