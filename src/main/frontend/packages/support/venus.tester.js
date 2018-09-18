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

    testCase: class testCase{

        /**
         * descriptor is a string which is used to see what the testCase is meant to test.
         * args should be a string or a list of strings
         * when is either a number of steps you want the sim to make or 'end' to run till the end (End = -1).
         * maxcycles should be used to stop the test if there may be an inf loop.
         */
        constructor(descriptor, args, when, maxcycles) {
            this.descriptor = descriptor;
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
            var tc = new window.tester.testCase(obj.descriptor, obj.args, obj.when, obj.maxcycles);
            tc.tests = obj.tests;
            return tc;
        }

        toString() {
            return JSON.stringify(this);
        }

        addArg(arg) {
            if (typeof arg === "string") {
                this.args.push(arg);
                return true;
            } else if (Array.isArray(arg)) {
                for (let i in arg) {
                    if (typeof arg[i] !== "string") {
                        arg[i] = arg[i].toString()
                    }
                }
                this.args = this.args.concat(arg);
                return true;
            }
            return false;
        }

        removeArgAt(index) {
            if (index >= 0 && index < this.args.length) {
                this.args.splice(index, 1);
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
            return true;
        }

        removeTest(id) {
            if (typeof this.tests[id] === "undefined") {
                return false;
            }
            delete this.tests[id];
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
                    case "reg":
                        var assertion = tester.assertRegister(sim, a, b);
                        break;
                    case "freg":
                        var assertion = tester.assertFRegister(sim, a, b);
                        break;
                    case "out":
                        var assertion = tester.assertOutput(sim, a, b);
                        break;
                    case "mem":
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
    openTester: function() {
        venus_main.venus.glue.Renderer.renderTab("tester", venus_main.venus.glue.Renderer.mainTabs);
    },
    insertAfter: function(newNode, referenceNode) {
        referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
    },
    tab: `<center><div class="tile is-ancestor">
  <div class="tile is-vertical">
    <div class="tile">
      <div class="tile is-parent">
          <article class="tile is-child is-primary" align="center">
            <font size="6px">Venus Code Tester</font><br>
            This is currently a work in progress. Please give me time!<br>
            The backend of this is what is mostly in, the front end will come with time (though I suck at front end :'( ).
          </article>
        </center>
      </div>
    </div>
    <br><br>
   </div>
  </div></center>`
};