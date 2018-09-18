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

    testCase: class testCase{

        /*When = number of steps before test check or 'end'*/
        constructor(descriptor) {
            this.descriptor = descriptor;
            this.tests = {};
        }

        addTest(id, description,  assertion, arg1, arg2) {
            if (typeof this.tests[id] !== "undefined") {
                return false;
            }
            this.tests.push(id, [description, assertion, arg1, arg2]);
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

        testAll() {
            let details = [];
            for (let testid in Object.keys(this.tests)) {
                let test = this.tests[testid];
                let f = test[1];
                let a = test[2];
                let b = test[3];
                details.push([testid, f(a, b)]);
            }
            return details;
        }
    },

    assertRegister: function(regID, expected){
        return driver.sim.getReg(regID) === expected;
    },

    assertFRegister: function(regID, expected) {
      return driver.sim.getFReg(regID) === expected;
    },

    assertOutput: function(_, expected){
        var console = document.getElementById("console-output");
        return console && console.value === expected;
    },

    assertMemory: function(address, expected) {
        return driver.sim.loadByte(address) === expected;
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
            This is currently a work in progress. Please give me time!
          </article>
        </center>
      </div>
    </div>
    <br><br>
   </div>
  </div></center>`
};