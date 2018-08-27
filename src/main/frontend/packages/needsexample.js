'use strict';
var venuspackage = {
    id: "requirespackage",
    requires: ["examplepackage"],
    load: function() {
        console.warn("I only work with examplepackage!.");
    },
    unload: function() {
        console.warn("I have unloaded the package! This is the stage which all of the changes and hooks should be undone.")
    }
};