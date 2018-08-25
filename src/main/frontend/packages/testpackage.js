'use strict';
var venuspackage = {
  id: "testpackage",
  load: function() {
      console.log("loadedfn");
  },
  unload: function() {
      console.log("unloaded testpackage")
  }
};