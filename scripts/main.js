function setup_venus() {
    console.log("----------THIS IS THE END OF THE EXPECTED GET ERRORS!----------");
    try {
        window.venus_main = window.venus;
        window.driver = venus_main.venus.Driver;
        window.simulatorAPI = venus_main.venus.api.venusbackend.simulator.Simulator;
        window.editor = document.getElementById("asm-editor");
        window.codeMirror = CodeMirror.fromTextArea(editor,
            {
                lineNumbers: true,
                mode: "venusbackend.riscv",
                indentUnit: 4,
                autofocus: true,
                lint: true
            }
        );
        window.codeMirror.setSize("100%", "88vh");
    } catch (e) {
        load_error(e.toString())
    }
}

function local_riscv() {
    loadScript("js/risc-mode.js", "var msg='COULD NOT LOAD RISCVMODE SCRIPT!';load_error(msg);", "local_kotlin();");
}

function local_kotlin() {
    // loadScript("https://try.kotlinlang.org/static/kotlin/1.3.11/kotlin.js", "alert('COULD NOT LOAD KOTLIN SCRIPT!');", "local_venus();");
    loadScript("../../../build/kotlin-js-min/main/kotlin.js", "var msg='COULD NOT LOAD KOTLIN SCRIPT!';load_error(msg);", "local_venus();");
}

function local_venus() {
    temp_fix();
    loadScript("../../../build/kotlin-js-min/main/venus.js", "var msg='COULD NOT LOAD VENUS SCRIPT!';load_error(msg);", "setup_venus();")
}

function deploy_venus() {
    temp_fix();
    loadScript("js/venus.js",  "var msg='COULD NOT LOAD VENUS SCRIPT!';load_error(msg);", "setup_venus();");
}

function main_venus() {
    loadScript("js/kotlin.js", "local_riscv();", "deploy_venus();");
}

function temp_fix() {
    if (typeof kotlin !== "undefined" && typeof kotlin.kotlin !== "undefined" && typeof kotlin.kotlin.Number === "undefined") {
        kotlin.kotlin.Number = function (){};
        kotlin.kotlin.Number.prototype.call = function(a){};
    }
}

function load_error(msg) {
    var e = document.getElementById("loading_text");
    e.innerHTML = "An error has occurred while loading Venus!";
    if (typeof msg == "string") {
        e.innerHTML += "<br><br><div style=\"font-size:0.75em\">" + msg.replace(/\n/g, '<br>') + "</div>";
    }
    e.innerHTML += "<br><br><br><div style=\"font-size:0.6em\">Try to reload the page. If that does not fix the issue, make an issue post on <a href='https://github.com/ThaumicMekanism/venus/issues'>github</a>.<font></font>";
    document.getElementById("loader").style.opacity = 0;
}

window.load_done = function () {
    window.document.body.classList.add("loaded");
    window.onerror = null;
};

window.onerror = function (message, source, lineno, colno, error) {
    load_error(message + "\nMore info in the console.")
};

main_venus();