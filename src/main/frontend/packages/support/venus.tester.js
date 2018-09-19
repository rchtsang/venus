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
            return this.testCases.push(testCase);
        },
        removeTestCase(index) {
            if (index >= 0 && index < this.testCases.length) {
                this.testCases.splice(index, 1);
                return true;
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
        constructor(descriptor, args, when, maxcycles) {
            this.id = descriptor;
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
            if (typeof arg === "string") {
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
         * @param sim - A venus simulator object.
         * @returns {boolean, [testid, boolean] ... }
         */
        testAll(sim) {
            /**
             * First step is to get the simulator into the correct state.
             * I am not resetting the sim since it assumes the sim is in the correct initial state. It will then add
             * args and process it.
             */
            for (let arg of this.args) {
                sim.addArg(arg);
            }
            while( !sim.isDone() && (sim.cycles < this.when || this.when === -1) && (sim.cycles < this.maxcycles || this.maxcycles === -1)) {
                sim.step();
            }

            /**
             * This part of the code will actually do the comparisons with the simulator.
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
                state = state && assertion;
                details.push([testid, assertion]);
            }
            return [state].concat(details);
        }
    },

    assertRegister: function(sim, regID, expected){
        return sim.getReg(regID) === expected;
    },

    assertFRegister: function(sim, regID, expected) {
      return sim.getFReg(regID) === expected;
    },

    assertOutput: function(sim, _, expected){
        var console = document.getElementById("console-output");
        return console && console.value === expected;
    },

    assertMemory: function(sim, address, expected) {
        return sim.loadByte(address) === expected;
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
            inpt.value = "?";

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
            //inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
            inpt.value = "";
            loc.appendChild(inpt);
            t.appendChild(loc);
            var exp = document.createElement("td");
            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            //inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
            inpt.value = "";
            exp.appendChild(inpt);
            t.appendChild(exp);
            var rm = document.createElement("td");
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.setAttribute("onclick", "tester.saveTest(this.parentElement.parentElement);");
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

    addNewTestCase() {
        var newTest = new this.testCase("Test", [], -1, -1);
        this.activeTest = newTest;
        var id = this.testingEnv.testCases.length;
        this.testingEnv.addTestCase(newTest);
        this.displayTest(newTest);
        this.addTestToSideBar(newTest, id);
    },

    addTestToSideBar(testCase, id) {
        idd = "testCase-" + id;
        var odiv = document.createElement("div");
        odiv.id = idd + "-div";
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
        lab.innerHTML = id;
        inadiv.appendChild(lab);
        mdiv.appendChild(inadiv);

        var inbdiv = document.createElement("div");
        inbdiv.classList.add("field-body");
        inbdiv.classList.add("is-expanded");

        var btnShow = document.createElement("button");
        btnShow.classList.add("button");
        btnShow.setAttribute("onclick", "tester.showTest(" + id + ");");
        btnShow.innerHTML = "Show";
        var btnRemove = document.createElement("button");
        btnRemove.classList.add("button");
        btnRemove.classList.add("is-primary");
        btnRemove.style.backgroundColor = "red";
        btnRemove.innerHTML = "Remove";
        btnRemove.setAttribute("onclick", "tester.removeTest(" + id + ");");

        inbdiv.appendChild(btnShow);
        inbdiv.appendChild(btnRemove);

        mdiv.appendChild(inbdiv);
        odiv.appendChild(mdiv);

        document.getElementById("testCases-list").appendChild(odiv);
    },

    consoleOut(text) {
        document.getElementById("console-out").value = text;
    },

    getConsoleOut() {
        return document.getElementById("console-out").value;
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
        inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
        inpt.value = testCase.id;

        des.appendChild(inpt);
        b.appendChild(des);
        var when = document.createElement("td");

        var inpt = document.createElement("input");
        inpt.setAttribute("class", "input is-small");
        inpt.setAttribute("spellcheck", "false");
        inpt.setAttribute("type", "number");
        inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
        inpt.value = testCase.when;

        when.appendChild(inpt);
        b.appendChild(when);

        var maxr = document.createElement("td");


        var inpt = document.createElement("input");
        inpt.setAttribute("class", "input is-small");
        inpt.setAttribute("spellcheck", "false");
        inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
        inpt.value = testCase.maxcycles;

        maxr.appendChild(inpt);
        b.appendChild(maxr);

        base.appendChild(b);

        var i = 0;
        while (i < testCase.args.length) {
            var a = document.createElement("tr");

            var id = document.createElement("td");
            id.innerHTML = i;
            a.appendChild(id);
            var ag = document.createElement("td");


            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
            inpt.value = testCase.args[i];

            ag.appendChild(inpt);
            a.appendChild(ag);
            var rm = document.createElement("td");
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.style.backgroundColor = "red";
            btn.setAttribute("onclick", "tester.consoleOut('TODO: REMOVE ME');");
            btn.innerHTML = "Remove";
            rm.appendChild(btn);
            a.appendChild(rm);

            args.appendChild(a);
            i++;
        }

        for (i of Object.keys(testCase.tests)) {
            var t = document.createElement("tr");

            var id = document.createElement("td");

            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
            inpt.value = i;

            id.appendChild(inpt);
            t.appendChild(id);

            var sel = document.createElement("select");
            sel.setAttribute("onchange", "tester.consoleOut('wip');");

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
            inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
            inpt.value = testCase.tests[i][1];
            loc.appendChild(inpt);
            t.appendChild(loc);
            var exp = document.createElement("td");
            var inpt = document.createElement("input");
            inpt.setAttribute("class", "input is-small");
            inpt.setAttribute("spellcheck", "false");
            inpt.setAttribute("onblur", "tester.consoleOut('WIP');");
            inpt.value = testCase.tests[i][2];
            exp.appendChild(inpt);
            t.appendChild(exp);
            var rm = document.createElement("td");
            var btn = document.createElement("button");
            btn.classList.add("button", "is-primary");
            btn.style.backgroundColor = "red";
            btn.setAttribute("onclick", "tester.consoleOut('TODO: REMOVE ME');");
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
    tab: `
  <div class="tile is-ancestor">
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
                <th>Remove?</th>
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
                <th>Remove?</th>
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
            <nav class="panel">
              <p class="panel-tabs">
                <a id="testCases-tab" class="is-active" onclick="window.tester.openTestCases();">Test Cases</a>
              </p>
              <nav id="testCases-tab-view" class="panel">
                <div id="reg-0c" class="panel-block">
                    <div class="field is-horizontal">
                      <div class="field-label">
                        <label class="label is-small" for="reg-0-vacl">Add Test Case</label>
                      </div>
                      <div class="field-body is-expanded" style="flex-grow: inherit;">
                        <button class="button is-primary" onclick="tester.addNewTestCase();">Add</button>
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