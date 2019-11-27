'use strict';
/**
 * You must make sure you have use strict in your package.
 * Also you must make sure the id is unique from other packages.
 * You need to make sure venuspackage is the name of the dictionary since Venus assumes that it is called that
 * and contains id (String), load (function(String)), and unload (function(String)). If you do not have one of these
 * fields, Venus will error!
 *
 * The string which the load and unload function take in will contain details about what venus wants the function to do.
 * On the load function, setting may contain:
 *  'enabled'   - This means you should load your package and enable it.
 *  'disabled'  - This means that you should only setup base stuff but leave your package in the disabled state.
 *
 * On the unload function, setting may contain:
 *  'disable'   - This means that you should disable your package.
 *  'remove'    - This means you should remove your package because venus is about to remove it.
 */
var venuspackage = {
    id: "chocopy",
    requires: undefined,
    name: "chocopy",
    firstload: true,
    load: function(setting) {
        if (setting.includes("enabled")) {
            if (this.firstload) {
                this.firstload = false;
                var body = window.venus.api.addMainTabAndShow(this.name);
                return chocopyenable(body);
            } else {
                return window.venus.api.showMainTab(this.name);
            }
        } else if (setting.includes("disabled")) {
            if (this.firstload) {
                this.firstload = false;
                var body = window.venus.api.addMainTab(this.name);
                return chocopyenable(body);
            }
        } else {
            return false;
        }
    },
    unload: function(setting) {
        if (setting.includes("disable")) {
            window.venus.api.hideMainTab(this.name);
            return true;
        } else if (setting.includes("remove")) {
            window.venus.api.removeMainTab(this.name);
            return true;
        } else {
            return false;
        }
    }
};

function chocopyenable(body) {
    if (body == null) {
        return false;
    }
    var baseurl = "packages/support/chocopy";
    body.innerHTML=`<object id="chocopy_fe" style="width:100%; height: 100%;background: transparent url(${baseurl}/img/AnimatedLoading.gif) no-repeat center;" type="text/html" data="${baseurl}/chocopy.html" ></object>`;
    body.style.width = "100%";
    body.style.height = "100%";
    return true;
}