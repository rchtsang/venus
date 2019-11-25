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
  load: function(setting) {
      console.warn("I have loaded the example package! This is the stage which would apply all of the hooks and code to interact with Venus.");
  },
  unload: function(setting) {
      console.warn("I have unloaded the test package! This is the stage which all of the changes and hooks should be undone.")
  }
};