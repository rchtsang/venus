window.VFS = class VirtualFileSystem {
    constructor() {
        this.fs = {};
    }

    addFile(filename, filecontents, filepath) {
        var actualPath = filepath.split("/");
        var location = this.fs;
        for (var folder of actualPath) {
            if (folder === "") {
                continue;
            }
            if (location[folder] === undefined) {
                location[folder] = {type:"folder", contents: {}};
                location = location[folder].contents;
            }
        }
        location[filename] = {type:"file", contents: filecontents};
    }

    addFolder(foldername, path) {
        var actualPath = filepath.split("/");
        var location = this.fs;
        for (var folder of actualPath) {
            if (folder === "") {
                continue;
            }
            if (location[folder] === undefined) {
                location[folder] = {type:"folder", contents: {}};
                location = location[folder].contents;
            }
        }
        location[filename] = {type:"folder", contents: {}};
    }

    readFile(filepath) {
        var actualPath = filepath.split("/");
        var filename = actualPath.pop();
        var location = this.fs;
        for (var folder of actualPath) {
            if (folder === "") {
                continue;
            }
            if (location[folder] === undefined) {
                return null;
            } else {
                location = location[folder].contents;
            }
        }
        if  (location[filename] === undefined) {
            return null;
        } else {
            return location[filename].contents;
        }
    }

    writeFile(filepath, filecontents) {
        var actualPath = filepath.split("/");
        var filename = actualPath.pop();
        this.addFile(filename, filecontents, actualPath.join("/"));
    }

    removeFile(filepath) {
        var actualPath = filepath.split("/");
        var filename = actualPath.pop();
        var location = this.fs;
        for (var folder of actualPath) {
            if (folder === "") {
                continue;
            }
            if (location[folder] === undefined) {
                return false;
            } else {
                location = location[folder].contents;
            }
        }
        return delete location[filename];
    }

    removeFolder(filepath) {
        this.removeFile(filepath)
    }
};