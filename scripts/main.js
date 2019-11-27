function setup_venus() {
    console.log("----------THIS IS THE END OF THE EXPECTED GET ERRORS!----------");
    try {
        load_update_message("Initializing codeMirror");
        window.venus_main = window.venus;
        window.driver = venus_main.venus.Driver;
        window.venus.api = venus_main.venus.api.API;
        window.simulatorAPI = venus_main.venus.api.venusbackend.simulator.Simulator;
        window.editor = document.getElementById("asm-editor");
        window.codeMirror = CodeMirror.fromTextArea(editor,
            {
                lineNumbers: true,
                mode: "riscv",
                indentUnit: 4,
                autofocus: true,
                lint: true
            }
        );
        window.codeMirror.setSize("100%", "88vh");
        window.codeMirror.refresh()
    } catch (e) {
        load_error(e.toString())
    }
}

function local_riscv() {
    load_update_message("Loading required file (local): js/risc-mode.js");
    loadScript("js/risc-mode.js", "var msg='COULD NOT LOAD RISCVMODE SCRIPT!';load_error(msg);", "local_kotlin();");
}

function local_kotlin() {
    load_update_message("Loading required file (local): kotlin.js");
    // loadScript("https://try.kotlinlang.org/static/kotlin/1.3.11/kotlin.js", "alert('COULD NOT LOAD KOTLIN SCRIPT!');", "local_venus();");
    loadScript("../../../build/kotlin-js-min/main/kotlin.js", "var msg='COULD NOT LOAD KOTLIN SCRIPT!';load_error(msg);", "local_venus();");
}

function local_venus() {
    load_update_message("Loading required file (local): venus.js");
    temp_fix();
    loadScript("../../../build/kotlin-js-min/main/venus.js", "var msg='COULD NOT LOAD VENUS SCRIPT!';load_error(msg);", "setup_venus();")
}

function deploy_venus() {
    load_update_message("Loading required file: venus.js");
    temp_fix();
    loadScript("js/venus.js",  "var msg='COULD NOT LOAD VENUS SCRIPT!';load_error(msg);", "setup_venus();");
}

function main_venus() {
    load_update_message("Loading required file: kotlin.js");
    loadScript("js/kotlin.js", "load_update_message(\"Loading required file: kotlin.js...FAILED!\");local_riscv();", "deploy_venus();");
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

window.driver_load_done = function () {
    /* Check if packages are all loaded */
    h  = function(){
        if (driver.driver_complete_loading) {
            load_done();
            return
        }
        setTimeout(h, 10);
    };
    setTimeout(h, 10);
};

window.load_done = function () {
    load_update_message("Done!");
    window.document.body.classList.add("loaded");
    window.onerror = null;
};

function load_update_message(msg) {
    document.getElementById("load-msg").innerHTML = msg.replace(/\n/g, "<br>");
}

function load_error_fn(message, source, lineno, colno, error) {
    load_error(message + "\nMore info in the console.");
}

window.onerror = load_error_fn;
window.default_alert = window.alert;
window.alert = alertify.alert;
// window.confirm = alertify.confirm;
// window.prompt = alertify.prompt;
alertify.alert()
    .setting({
        'title': 'Venus'
    });
alertify.confirm()
    .setting({
        'title': 'Venus'
    });
alertify.prompt()
    .setting({
        'title': 'Venus'
    });

main_venus();