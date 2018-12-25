var tester = {
    /*This code is used to initialize, enable, and disable the testing environment.*/
    initialize: function(state) {
        if (state === "enabled") {
            this.enable();
        }
    },
    enable: function() {
        var lielem = document.createElement('li');
        var aelem = document.createElement('a');
        aelem.setAttribute("onclick", "window.tester.openTester()");
        lielem.setAttribute("id", "tester-tab");
        var secelem = document.createElement('section');
        secelem.setAttribute("class", "section");
        secelem.setAttribute("id", "tester-tab-view");
        secelem.style.display = "none";
        secelem.innerHTML = this.tab;
        aelem.innerHTML = "Tester";
        lielem.appendChild(aelem);
        document.getElementsByClassName('tabs')[0].children[0].appendChild(lielem);
        this.insertAfter(secelem, document.getElementById("simulator-tab-view"));
        venus_main.venus.glue.Renderer.addTab("tester", venus_main.venus.glue.Renderer.mainTabs);
    },
    disable: function() {
        venus_main.venus.glue.Renderer.removeTab("tester", venus_main.venus.glue.Renderer.mainTabs);
        if (document.getElementById("tester-tab").classList.contains("is-active")) {
            venus_main.venus.glue.Renderer.renderTab("editor", venus_main.venus.glue.Renderer.mainTabs);
        }
        document.getElementById("tester-tab").remove();
        document.getElementById("tester-tab-view").remove();
    },

    testingEnv: {
        testCases: [],
        addTestCase(testCase) {
            if (this.hasTestID(testCase.id) === false) {
                return this.testCases.push(testCase);
            }
            return false;
        },
        removeTestID(id) {
            for (var i = 0; i < this.testCases.length; i++) {
                if (id === this.testCases[i].id) {
                    return this.removeTestCase(i);
                }
            }
            return false;
        },

        hasTestID(id) {
            for (var i = 0; i < this.testCases.length; i++) {
                if (id === this.testCases[i].id) {
                    return i;
                }
            }
            return false
        },

        getTestFromId(id){
            var index = this.hasTestID(id);
            if (index !== false) {
                return this.testCases[index];
            }
            return false;
        },

        removeTestCase(index) {
            if (index >= 0 && index < this.testCases.length) {
                return this.testCases.splice(index, 1);
            }
            return false;
        },
        removeAllTestCases() {
            this.testCases = [];
        },
        exportTests() {
            return JSON.stringify(this.testCases);
        },
        /**
         * This imports the JSON stringify tests
         * @param tests -> A json (so it is a string) list of testCases as strings
         */
        importTests(tests) {
            tcs = JSON.parse(tests);
            for (t of tcs) {
                let tc = window.tester.testCase.parseTestCase(t);
                this.addTestCase(tc);
            }
        },
        testAll(program) {
            var results = [];
            var passed = true;
            var i = 0;
            while (i < this.testCases.length) {
                var sim = driver.externalAssemble(program);
                if (!sim[0]) {
                    return [false, ["ERROR! Could not assemble text!", false]];
                }
                let tmp = this.testCases[i].testAll(sim[1]);
                passed = passed && tmp[0];
                results.push(tmp);
                document.getElementById("console-output").value = "";
                i++;
            }
            return [passed].concat(results);
        },
        testAllWithSim(baseSim) {
            var results = [];
            var passed = true;
            var i = 0;
            while (i < this.testCases.length) {
                let tmp = this.testCases[i].testAll(baseSim);
                passed = passed && tmp[0];
                results.push(tmp);
                baseSim.reset();
                document.getElementById("console-output").value = "";
                i++;
            }
            return [passed].concat(results);
        }
    },

    testcounter: 0,

    testCase: class testCase{
        /**
         * id is a string which is used to see what the testCase is meant to test.
         * args should be a string or a list of strings
         * when is either a number of steps you want the sim to make or 'end' to run till the end (End = -1).
         * maxcycles should be used to stop the test if there may be an inf loop.
         */
        constructor(id, args, when, maxcycles) {
            this.id = id;
            if (typeof args === "string") {
                this.args = [args];
            } else if (Array.isArray(args)) {
                for (let i in args) {
                    if (typeof args[i] !== "string") {
                        args[i] = args[i].toString();
                    }
                }
                this.args = args;
            } else {
                this.args = [];
            }
            this.tests = {};
            this.numTests = 0;
            this.numArgs = 0;
            if (typeof when === "undefined") {
                this.when = -1;
            } else {
                if (typeof when === "number") {
                    this.when = when;
                } else {
                    this.when = -1;
                }
            }
            if (typeof maxcycles === "undefined") {
                this.maxcycles = -1;
            } else {
                if (typeof maxcycles === "number") {
                    this.maxcycles = maxcycles;
                } else {
                    this.maxcycles = -1;
                }
            }
        }

        static parseTestCase(jsonString) {
            if (typeof jsonString === "string") {
                var obj = JSON.parse(jsonString);
            } else if(typeof jsonString === "object") {
                var obj = jsonString;
            }
            var tc = new window.tester.testCase(obj.id, obj.args, obj.when, obj.maxcycles);
            tc.tests = obj.tests;
            return tc;
        }

        toString() {
            return JSON.stringify(this);
        }

        addArg(arg) {
            if (typeof arg === "string" && arg !== "") {
                this.args.push(arg);
                this.numArgs++;
                return true;
            } else if (Array.isArray(arg)) {
                for (let i in arg) {
                    if (typeof arg[i] !== "string") {
                        arg[i] = arg[i].toString()
                    }
                    this.numArgs++;
                }
                this.args = this.args.concat(arg);
                return true;
            }
            return false;
        }

        removeArgAt(index) {
            if (index >= 0 && index < this.args.length) {
                this.args.splice(index, 1);
                this.numArgs--;
                return true;
            }
            return false;
        }

        removeArg(arg) {
            return this.removeArgAt(this.args.indexOf(arg))
        }

        hasTestID(id) {
            return typeof this.tests[id] !== "undefined";
        }

        addTest(id, assertion, arg1, arg2) {
            if (typeof this.tests[id] !== "undefined") {
                return false;
            }
            this.tests[id] = [assertion, arg1, arg2];
            this.numTests++;
            return true;
        }

        removeTest(id) {
            if (typeof this.tests[id] === "undefined") {
                return false;
            }
            delete this.tests[id];
            this.numTests--;
            return true;
        }

        listTests() {
            return this.tests;
        }

        /**
         * The first thing it returns is if all of the tests succeeded. Then there is a list of the test which passed/failed.
         * @param sim - A venus venusbackend.simulator object.
         * @returns {boolean, [testid, boolean] ... }
         */
        testAll(sim) {
            /**
             * First step is to get the venusbackend.simulator into the correct state.
             * I am not resetting the sim since it assumes the sim is in the correct initial state. It will then add
             * args and process it.
             */
            for (let arg of this.args) {
                sim.addArg(arg);
            }
            while( !sim.isDone() && (sim.cycles < this.when || this.when < 0) && (sim.cycles < this.maxcycles || this.maxcycles < 0)) {
                sim.step();
            }

            /**
             * This part of the code will actually do the comparisons with the venusbackend.simulator.
             * @type {Array}
             */
            let details = [];
            var state = true;
            for (let testid of Object.keys(this.tests)) {
                let test = this.tests[testid];
                let f = test[0];
                let a = test[1];
                let b = test[2];
                switch (f) {
                    case "register":
                        var assertion = tester.assertRegister(sim, a, b);
                        break;
                    case "fregegister":
                        var assertion = tester.assertFRegister(sim, a, b);
                        break;
                    case "output":
                        var assertion = tester.assertOutput(sim, a, b);
                        break;
                    case "memory":
                        var assertion = tester.assertMemory(sim, a, b);
                        break;
                    default:
                        var assertion = false;
                }
                state = state && assertion[0];
                var result = [testid, assertion[0]];
                if (!assertion[0]) {
                    result.push("Expected: " + b + "; Actual: " + assertion[1] + ";");
                }
                details.push(result);
            }
            return [state, this.id].concat([details]);
        }
    },

    assertRegister: function(sim, regID, expected){
        var result = [sim.getReg(regID) === expected, sim.getReg(regID)];
        return result;
    },

    assertFRegister: function(sim, regID, expected) {
        var result = [sim.getFReg(regID) === expected, sim.getFReg(regID)];
        return result;
    },

    assertOutput: function(sim, _, expected){
        var console = document.getElementById("console-output");
        var result = [console && console.value === expected];
        if (!console) {
            result.push("Could not find console output!");
        } else {
            result.push(console.value);
        }
        return result;
    },

    assertMemory: function(sim, address, expected) {
        var result = [sim.loadByte(address) === expected, sim.loadByte(address)];
        return result;
    },

    /*This is the code to manage the tab view.*/
    exportAllTests() {
        var tsts = this.testingEnv.exportTests();
        this.consoleOut(tsts);
    },

    exportCurrentTest() {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var tst = this.activeTest.toString();
            this.consoleOut(tst);
        }
    },

    testCurrent() {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            codeMirror.save();
            var prog = driver.getText();
            var sim = driver.externalAssemble(prog);
            if (!sim[0]) {
                this.consoleOut("ERROR! Could not assemble text!");
                return;
            }
            this.consoleOut(JSON.stringify(this.activeTest.testAll(sim[1])));
        }
    },

    testAll() {
        codeMirror.save();
        var prog = driver.getText();
        this.consoleOut(JSON.stringify(this.testingEnv.testAll(prog)));
    },

    addNewTestCheck() {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var t = document.createElement("tr");

            var id = document.createElement("td");

            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.value = "";

            id.appendChild(inpt);
            t.appendChild(id);
            var type = document.createElement("td");

            var sel = document.createElement("select");

            var r = document.createElement("option");
            r.innerText = "register";
            r.selected = true;
            sel.appendChild(r);
            var fr = document.createElement("option");
            fr.innerText = "fregister";
            sel.appendChild(fr);
            var o = document.createElement("option");
            o.innerText = "output";
            sel.appendChild(o);
            var m = document.createElement("option");
            m.innerText = "memory";
            sel.appendChild(m);

            var seltd = document.createElement("td");
            var seldiv = document.createElement("div");

            seldiv.setAttribute("class", "select is-small");

            seldiv.appendChild(sel);
            type.appendChild(seldiv);
            t.appendChild(type);
            var loc = document.createElement("td");
            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.setAttribute("type", "number");
            inpt.value = "";
            loc.appendChild(inpt);
            t.appendChild(loc);
            var exp = document.createElement("td");
            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.value = "";
            exp.appendChild(inpt);
            t.appendChild(exp);
            var rm = document.createElement("td");
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.setAttribute("onclick", "tester.saveSubTest(this.parentElement.parentElement);");
            btn.innerHTML = "Save";
            rm.appendChild(btn);
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.setAttribute("onclick", "var elm = this.parentElement.parentElement;elm.parentNode.removeChild(elm);");
            btn.innerHTML = "Remove";
            btn.style.backgroundColor = "red";
            rm.appendChild(btn);
            t.appendChild(rm);

            document.getElementById("testCase-Test-body").appendChild(t);
        }
    },
    activeTest: null,

    addNewArg() {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var a = document.createElement("tr");

            var id = document.createElement("td");
            id.innerHTML = "?";
            a.appendChild(id);
            var ag = document.createElement("td");


            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            //inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
            inpt.value = "";

            ag.appendChild(inpt);
            a.appendChild(ag);
            var rm = document.createElement("td");
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.setAttribute("onclick", "tester.saveArg(this.parentElement.parentElement);");
            btn.innerHTML = "Save";
            rm.appendChild(btn);
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.setAttribute("onclick", "var elm = this.parentElement.parentElement;elm.parentNode.removeChild(elm)");
            btn.innerHTML = "Remove";
            btn.style.backgroundColor = "red";
            rm.appendChild(btn);
            a.appendChild(rm);

            document.getElementById("testCase-Args-body").appendChild(a);
        }
    },

    removeTest(elm) {
        var id = elm.children[0].children[0].children[0].innerHTML;
        var rtc = this.testingEnv.removeTestID(id)[0];
        if (rtc !== false && rtc == this.activeTest) {
            this.clearTestDisplay();
        }
        elm.parentNode.removeChild(elm);
    },

    showTest(elm) {
        var id = elm.children[0].children[0].children[0].innerHTML;
        var tstcs = this.testingEnv.getTestFromId(id);
        if (tstcs === false) {
            this.consoleOut("Cound not find element in ENV!");
        } else {
            this.displayTest(tstcs);
        }
    },

    saveArg(parentelm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            argID = parentelm.children[0];
            argELM = parentelm.children[1];
            argBTN = parentelm.children[2];

            if (this.activeTest.addArg(argELM.children[0].value)) {
                argID.innerHTML = this.activeTest.args.length - 1;
                argBTN.children[1].setAttribute("onclick", "tester.displayRemoveArg(this.parentElement.parentElement);");
                argBTN.removeChild(argBTN.children[0]);
            }
        }
    },

    addNewTestCase() {
        var testID;
        do {
            this.testcounter++;
            testID = "Test-" + this.testcounter;
        } while(this.testingEnv.hasTestID(testID) !== false);
        var newTest = new this.testCase(testID, [], -1, -1);
        this.activeTest = newTest;
        var success = this.testingEnv.addTestCase(newTest);
        if (success !== false) {
            this.displayTest(newTest);
            this.addTestToSideBar(newTest);
        }
    },

    addTestToSideBar(testCase) {
        idd = "testCase-div-" + testCase.id;
        var odiv = document.createElement("div");
        odiv.id = idd;
        odiv.classList.add("panel-block");

        var mdiv = document.createElement("div");
        mdiv.classList.add("field");
        mdiv.classList.add("is-horizontal");

        var inadiv = document.createElement("div");
        inadiv.classList.add("field-label");

        var lab = document.createElement("label");
        lab.classList.add("label");
        lab.classList.add("is-small");
        lab.for = idd;
        lab.innerHTML = testCase.id;
        inadiv.appendChild(lab);
        mdiv.appendChild(inadiv);

        var inbdiv = document.createElement("div");
        inbdiv.classList.add("field-body");
        inbdiv.classList.add("is-expanded");

        var btnShow = document.createElement("button");
        btnShow.classList.add("button");
        btnShow.setAttribute("onclick", "tester.showTest(this.parentElement.parentElement.parentElement);");
        btnShow.innerHTML = "Show";
        var btnRemove = document.createElement("button");
        btnRemove.classList.add("button");
        btnRemove.classList.add("is-primary");
        btnRemove.style.backgroundColor = "red";
        btnRemove.innerHTML = "Remove";
        btnRemove.setAttribute("onclick", "tester.removeTest(this.parentElement.parentElement.parentElement);");

        inbdiv.appendChild(btnShow);
        inbdiv.appendChild(btnRemove);

        mdiv.appendChild(inbdiv);
        odiv.appendChild(mdiv);

        document.getElementById("testCases-list").appendChild(odiv);
    },

    importNewTestCase() {
        var testortests = prompt("Please enter the test case or cases to import. Leave blank to do nothing");
        if (testortests !== "") {
            try{
                var jsonparsed = JSON.parse(testortests);
                if (typeof jsonparsed === "object") {
                    var parseTC = function(jsontc) {
                        var tc = tester.testCase.parseTestCase(jsontc);
                        if (tester.testingEnv.addTestCase(tc) === false) {
                            var pconsole = tester.getConsoleOut();
                            tester.consoleOut(pconsole + "Could not add test!\n" + JSON.stringify(tc) + "\n")
                        } else {
                            tester.addTestToSideBar(tc);
                        }
                    };
                    tester.consoleOut("");
                    if (Array.isArray(jsonparsed)) {
                        //This is a batch of tests for env
                        for (var tc of jsonparsed) {
                            try {
                                parseTC(tc);
                            } catch (e) {
                                console.error(e);
                                var pconsole = this.getConsoleOut();
                                this.consoleOut(pconsole + "Could not parse some of the tests!\n");
                            }
                        }
                    } else {
                        //This is a single test for env
                        parseTC(jsonparsed);
                    }
                } else {
                    this.consoleOut("Unknown import data!");
                }
            } catch (e) {
                this.consoleOut("Could not parse the input!");
            }
        }
    },

    saveID(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var newID = elm.value;
            var prevID = elm.prevValue;
            if (newID !== elm.prevValue) {
                if (this.testingEnv.hasTestID(newID) === false) {
                    elm.prevValue = newID;

                    this.activeTest.id = newID;

                    var sidebarelm = document.getElementById("testCase-div-" + prevID);
                    sidebarelm.id = "testCase-div-" + newID;
                    sidebarelm.children[0].children[0].children[0].innerHTML = newID;
                } else {
                    this.consoleOut("The Test ID already exists! ID's must be unique!");
                    elm.value = prevID;
                }
            } else {
                elm.value = prevID;
            }
        }
    },

    saveWhen(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            try {
                var w = parseInt(elm.value);

                if (isNaN(w)) {
                    if (elm.value === "") {
                        elm.value = elm.prevValue;
                    } else {
                        throw EvalError("Could not parse the input!");
                    }
                }

                this.activeTest.when = w;
                elm.value = w;
                elm.prevValue = w;
            } catch (e) {
                elm.value = elm.prevValue;
                this.consoleOut(e.toString());
            }
        }
    },

    saveMaxSteps(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            try {
                var w = parseInt(elm.value);

                if (isNaN(w)) {
                    if (elm.value === "") {
                        elm.value = elm.prevValue;
                    } else {
                        throw EvalError("Could not parse the input!");
                    }
                }

                this.activeTest.maxcycles = w;
                elm.value = w;
                elm.prevValue = w;
            } catch (e) {
                elm.value = elm.prevValue;
                this.consoleOut(e.toString());
            }
        }
    },

    saveSubTest(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var testidelm = elm.children[0].children[0];
            var testid = testidelm.value;
            if (testid === "") {
                this.consoleOut("The SubTestID must not be blank!");
                return;
            }
            if (this.activeTest.hasTestID(testid) === false) {
                var sel = elm.children[1].children[0].children[0];
                var testingWhat =  sel.options[sel.selectedIndex].value;
                var locelm = elm.children[2].children[0];
                var location;
                var expelm = elm.children[3].children[0];
                var expected;
                switch (testingWhat) {
                    case "register":
                        location = parseInt(locelm.value);
                        if (isNaN(location) || location < 0 || location > 31) {
                            this.consoleOut("You must select a register value within [0, 31]");
                            return;
                        }
                        expected = parseInt(expelm.value);
                        if (isNaN(expected) || expected < 0) {
                            this.consoleOut("You must have a positive integer value (for now);");
                            return;
                        }
                        break;
                    case "fregister":
                        location = parseInt(locelm.value);
                        if (isNaN(location) || location < 0 || location > 31) {
                            this.consoleOut("You must select a floating register value within [0, 31]");
                            return;
                        }
                        expected = parseInt(expelm.value);
                        if (isNaN(expected) || expected < 0) {
                            this.consoleOut("You must have a positive integer value (for now);");
                            return;
                        }
                        break;
                    case "output":
                        location = locelm.value;
                        expected = expelm.value;
                        break;
                    case "memory":
                        location = parseInt(locelm.value);
                        if (isNaN(location) || location < 0) {
                            this.consoleOut("You must select a valid memory address [0, MaxMemSpace).");
                            return;
                        }
                        expected = parseInt(expelm.value);
                        if (isNaN(expected) || expected < 0) {
                            this.consoleOut("You must have a positive integer value (for now);");
                            return;
                        }
                        break;
                    default:
                        this.consoleOut("Unknown thing to test!");
                        return;
                }

                testidelm.prevValue = testid;

                testidelm.setAttribute("onblur", "tester.saveSubTestID(this);");
                expelm.prevValue = expected;
                expelm.setAttribute("onblur", "tester.saveSubTestExpected(this);");
                locelm.prevValue = location;
                locelm.setAttribute("onblur", "tester.saveSubTestLocation(this);");
                sel.prevValue = testingWhat;
                sel.setAttribute("onchange", "tester.saveSubTestSelect(this);");
                elm.children[4].removeChild(elm.children[4].children[0]);
                elm.children[4].children[0].setAttribute("onclick", "tester.removeSubTest(this);")
                this.activeTest.addTest(testid, testingWhat, location, expected);
            } else {
                this.consoleOut("Your SubTestID must be unique;");
            }
        }
    },

    removeSubTest(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var node = elm.parentNode.parentNode;
            var id = node.children[0].children[0].value;
            this.activeTest.removeTest(id);
            node.parentNode.removeChild(node);
        }
    },

    saveSubTestID(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var newID = elm.value;
            var prevID = elm.prevValue;
            if (newID === prevID) {
                return;
            }
            if (this.activeTest.hasTestID(newID) === false) {
                var tc = this.activeTest.tests[prevID];
                if (tc === undefined) {
                    this.consoleOut("The testid could not be changed because it could not be found!");
                    return;
                }
                delete this.activeTest.tests[prevID];
                this.activeTest.tests[newID] = tc;
                elm.prevValue = newID;
            } else {
                elm.value = elm.prevValue;
                this.consoleOut("The newID must be unique!");
            }
        }
    },


    validSelectElms: new Set(["register", "fregister", "output", "memory"]),
    saveSubTestSelect(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var activeVal = elm.options[elm.selectedIndex].value;
            if (this.validSelectElms.has(activeVal)) {
                var id = elm.parentNode.parentNode.parentNode.children[0].children[0].value;
                var tc = this.activeTest.tests[id];
                if (tc === undefined) {
                    this.consoleOut("The testid could not be found!");
                    return;
                }
                tc[0] = activeVal;
                elm.prevValue = activeVal;
            } else {
                this.consoleOut("Unrecognized Selection!")
            }
        }
    },

    saveSubTestLocation(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var newLoc = elm.value;
            var id = elm.parentNode.parentNode.children[0].children[0].value;
            var tc = this.activeTest.tests[id];
            switch (tc[0]) {
                case "register":
                    newLoc = parseInt(newLoc);
                    if(isNaN(newLoc) || newLoc < 0 || newLoc > 31) {
                        this.consoleOut("The register must be in range!");
                        elm.value = elm.prevValue;
                        return;
                    }
                    break;
                case "fregister":
                    newLoc = parseInt(newLoc);
                    if(isNaN(newLoc) || newLoc < 0 || newLoc > 31) {
                        this.consoleOut("The floating register must be in range!");
                        elm.value = elm.prevValue;
                        return;
                    }
                    break;
                case "memory":
                    newLoc = parseInt(newLoc);
                    if(isNaN(newLoc) || newLoc < 0) {
                        this.consoleOut("The register must be a positive integer!");
                        elm.value = elm.prevValue;
                        return;
                    }
                    break;
                default:
            }
            tc[1] = newLoc;
            elm.value = newLoc;
            elm.prevValue = newLoc;
        }
    },

    saveSubTestExpected(elm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            var newExp = elm.value;
            var id = elm.parentNode.parentNode.children[0].children[0].value;
            var tc = this.activeTest.tests[id];
            switch (tc[0]) {
                case "register":
                    newExp = parseInt(newExp);
                    if(isNaN(newExp) || newExp < 0) {
                        this.consoleOut("The register must be a positive integer.");
                        elm.value = elm.prevValue;
                        return;
                    }
                    break;
                case "fregister":
                    newExp = parseFloat(newExp);
                    if(isNaN(newExp)) {
                        this.consoleOut("The floating register must be a valid float!");
                        elm.value = elm.prevValue;
                        return;
                    }
                    break;
                case "memory":
                    newExp = parseInt(newExp);
                    if(isNaN(newExp) || newExp < 0) {
                        this.consoleOut("The register must be a positive integer!");
                        elm.value = elm.prevValue;
                        return;
                    }
                    break;
                default:
            }
            tc[2] = newExp;
            elm.value = newExp;
            elm.prevValue = newExp;
        }
    },

    consoleOut(text) {
        document.getElementById("console-out").value = text;
    },

    getConsoleOut() {
        return document.getElementById("console-out").value;
    },

    displayArgs(args) {
        var argsBody = document.getElementById("testCase-Args-body");
        var i = 0;
        while (i < args.length) {
            var a = document.createElement("tr");

            var id = document.createElement("td");
            id.innerHTML = i;
            a.appendChild(id);
            var ag = document.createElement("td");


            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
            inpt.value = args[i];

            ag.appendChild(inpt);
            a.appendChild(ag);
            var rm = document.createElement("td");
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.style.backgroundColor = "red";
            btn.setAttribute("onclick", "tester.displayRemoveArg(this.parentElement.parentElement);");
            btn.innerHTML = "Remove";
            rm.appendChild(btn);
            a.appendChild(rm);

            argsBody.appendChild(a);
            i++;
        }
    },

    displayRemoveArg(parentelm) {
        if (this.activeTest === null) {
            this.consoleOut("NO ACTIVE TESTS!");
        } else {
            argID = parentelm.children[0];
            argELM = parentelm.children[1];
            argBTN = parentelm.children[2];

            this.activeTest.removeArgAt(argID.innerHTML);

            this.displayRemoveAllArgs(true);
            var argsBody = document.getElementById("testCase-Args-body");
            var temp = argsBody.innerHTML;
            argsBody.innerHTML = "";
            this.displayArgs(this.activeTest.args);
            argsBody.insertAdjacentHTML('beforeend', temp);
        }
    },

    displayRemoveAllArgs(onlyActive) {
        var argsBody = document.getElementById("testCase-Args-body");
        if (onlyActive) {
            var childrenToRemove = [];
            for (child of argsBody.children) {
                if (child.children[2].children[0].innerText !== "Save") {
                    childrenToRemove.push(child);
                }
            }
            for (child of childrenToRemove) {
                argsBody.removeChild(child);
            }
        } else {
            argsBody.innerHTML = "";
        }
    },

    displayTest(testCase) {
        this.clearTestDisplay();
        var base = document.getElementById("testCase-Base-body");
        var args = document.getElementById("testCase-Args-body");
        var test = document.getElementById("testCase-Test-body");

        var b = document.createElement("tr");
        var des = document.createElement("td");

        var inpt = document.createElement("input");
        inpt.setAttribute("class", "input is-small");
        inpt.setAttribute("spellcheck", "false");
        inpt.setAttribute("onblur", "tester.saveID(this);");
        inpt.value = testCase.id;
        inpt.prevValue = testCase.id;

        des.appendChild(inpt);
        b.appendChild(des);
        var when = document.createElement("td");

        var inpt = document.createElement("input");
        inpt.setAttribute("class", "input is-small");
        inpt.setAttribute("spellcheck", "false");
        inpt.setAttribute("type", "number");
        inpt.setAttribute("min", "-1");
        inpt.setAttribute("onblur", "tester.saveWhen(this);");
        inpt.value = testCase.when;
        inpt.prevValue = inpt.value;

        when.appendChild(inpt);
        b.appendChild(when);

        var maxr = document.createElement("td");


        var inpt = document.createElement("input");
        inpt.setAttribute("class", "input is-small");
        inpt.setAttribute("spellcheck", "false");
        inpt.setAttribute("type", "number");
        inpt.setAttribute("min", "-1");
        inpt.setAttribute("onblur", "tester.saveMaxSteps(this);");
        inpt.value = testCase.maxcycles;
        inpt.prevValue = inpt.value;

        maxr.appendChild(inpt);
        b.appendChild(maxr);

        base.appendChild(b);

        this.displayArgs(testCase.args);

        for (i of Object.keys(testCase.tests)) {
            var t = document.createElement("tr");

            var id = document.createElement("td");

            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.setAttribute("onblur", "tester.saveSubTestID(this);");
            inpt.value = i;

            id.appendChild(inpt);
            t.appendChild(id);

            var sel = document.createElement("select");
            sel.setAttribute("onchange", "tester.saveSubTestSelect(this);");

            var r = document.createElement("option");
            r.innerText = "register";
            sel.appendChild(r);
            var fr = document.createElement("option");
            fr.innerText = "fregister";
            sel.appendChild(fr);
            var o = document.createElement("option");
            o.innerText = "output";
            sel.appendChild(o);
            var m = document.createElement("option");
            m.innerText = "memory";
            sel.appendChild(m);
            
            switch (testCase.tests[i][0]) {
                case "fregister":
                    fr.selected = true;
                    break;
                case "output":
                    o.selected = true;
                    break;
                case "memory":
                    m.selected = true;
                    break;
                default:
                    r.selected = true;
            }

            var seltd = document.createElement("td");
            var seldiv = document.createElement("div");

            seldiv.setAttribute("class", "select is-small");

            seldiv.appendChild(sel);
            seltd.appendChild(seldiv);

            t.appendChild(seltd);

            var loc = document.createElement("td");
            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.setAttribute("type", "number");
            inpt.setAttribute("onblur", "tester.saveSubTestLocation(this);");
            inpt.value = testCase.tests[i][1];
            loc.appendChild(inpt);
            t.appendChild(loc);
            var exp = document.createElement("td");
            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.setAttribute("onblur", "tester.saveSubTestExpected(this);");
            inpt.value = testCase.tests[i][2];
            exp.appendChild(inpt);
            t.appendChild(exp);
            var rm = document.createElement("td");
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.style.backgroundColor = "red";
            btn.setAttribute("onclick", "tester.removeSubTest(this);");
            btn.innerHTML = "Remove";
            rm.appendChild(btn);
            t.appendChild(rm);

            test.appendChild(t);
        }
        this.activeTest = testCase;
    },

    clearTestDisplay() {
        var base = document.getElementById("testCase-Base-body");
        var args = document.getElementById("testCase-Args-body");
        var test = document.getElementById("testCase-Test-body");

        base.innerHTML = "";
        args.innerHTML = "";
        test.innerHTML = "";

        this.activeTest = null;
    },

    infoTabs: ["testCases"],
    openTestCases: function() {
        this.openTab("testCases", this.infoTabs);
    },
    openTab: function(tabName, tabsList) {
        for (t of tabsList) {
            if (t === tabName) {
                venus_main.venus.glue.Renderer.tabSetVisibility(t, "block");
            } else {
                venus_main.venus.glue.Renderer.tabSetVisibility(t, "none");
            }
        }
    },
    openTester: function() {
        venus_main.venus.glue.Renderer.renderTab("tester", venus_main.venus.glue.Renderer.mainTabs);
    },
    insertAfter: function(newNode, referenceNode) {
        referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
    },
    tab: `<div class="tile is-ancestor">
    <div class="tile is-vertical is-8">
      <div class="tile">
        <div class="tile is-parent">
          <article class="tile is-child is-primary" id="simulator-controls-container">
            <div class="field is-grouped is-grouped-centered">
              <div class="control">
                <button id="tester-testAll" class="button is-primary" onclick="tester.testAll();">Test All</button>
              </div>
              <div class="control">
                <button id="tester-testSelected" class="button" onclick="tester.testCurrent();">Test Selected</button>
              </div>
              <div class="control">
                <button id="tester-exportSelectedTest" class="button" onclick="tester.exportCurrentTest();">Export Selected Test</button>
              </div>
              <div class="control">
                <button id="tester-exportAllTests" class="button is-primary" onclick="tester.exportAllTests();">Export All Tests</button>
              </div>
            </div>
          </article>
        </div>
      </div>
      <div class="tile">
        <div class="tile is-parent">
          <article class="tile is-child is-primary" id="program-listing-container">
            <table id="testCase-Base" class="table">
              <colgroup>
                <col id="mc-column">
                <col id="bc-column">
                <col id="oc-column">
              </colgroup>
              <thead>
              <tr>
                <th>ID</th>
                <th>When To Test</th>
                <th>Maximum Cycles</th>
              </tr>
              </thead>
              <tbody id="testCase-Base-body">
              </tbody>
            </table>
            <table id="testCase-Args" class="table">
              <colgroup>
                <col id="mc-column">
                <col id="bc-column">
                <col id="oc-column">
              </colgroup>
              <thead>
              <tr>
                <th>Arg ID</th>
                <th>Argument</th>
                <th></th>
              </tr>
              </thead>
              <tbody id="testCase-Args-body">
              </tbody>
            </table>
              <button class="button is-primary" onclick="window.tester.addNewArg();">Add</button>
            <table id="testCase-Test" class="table">
              <colgroup>
                <col id="mc-column">
                <col id="bc-column">
                <col id="oc-column">
              </colgroup>
              <thead>
              <tr>
                <th>Test ID</th>
                <th>Testing What</th>
                <th>At Location</th>
                <th>Expected Value</th>
                <th></th>
              </tr>
              </thead>
              <tbody id="testCase-Test-body">
              </tbody>
            </table>
              <button class="button is-primary" onclick="window.tester.addNewTestCheck();">Add</button>
          </article>
        </div>
      </div>
      <div class="tile is-parent" align="center">
        <article class="tile is-child">
          <a onclick="CopyToClipboard('console-out')">Copy!</a>
          &nbsp;&nbsp;&nbsp;&nbsp;
          <a onclick="downloadtrace('console-out', 'console.out', true)">Download!</a>
          &nbsp;&nbsp;&nbsp;&nbsp;
          <a onclick="document.getElementById('console-out').value=''">Clear!</a>
          <br>
          <textarea id="console-out" class="textarea" placeholder="console output" readonly></textarea>
          <br>
        </article>
      </div>
    </div>
    <div class="tile is-ancestor">
      <div class="tile is-vertical">
        <div class="tile is-parent">
          <article class="tile is-child" id="sidebar-listings-container">
            <nav class="panel" id="tester-panel">
              <p class="panel-tabs" id="tester-panel-tabs">
                <a id="testCases-tab" class="is-active" onclick="window.tester.openTestCases();">Test Cases</a>
              </p>
              <nav id="testCases-tab-view" class="panel">
                <div id="add-test-case-div" class="panel-block">
                    <div class="field is-horizontal">
                      <div class="field-label">
                        <label class="label is-small" for="reg-0-vacl">Add Test Case</label>
                      </div>
                      <div class="field-body is-expanded" style="flex-grow: inherit;">
                        <button class="button is-primary" onclick="tester.addNewTestCase();">Add</button>
                        <button class="button" onclick="tester.importNewTestCase();">Import</button>
                      </div>
                    </div>
                  </div>
                  <div id="testCases-list">
                      
                  </div>
              </nav>
            </nav>
          </article>
        </div>
      </div>
    </div>
  </div>`
};