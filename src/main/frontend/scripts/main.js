function setup_venus() {
    console.log("----------THIS IS THE END OF THE EXPECTED GET ERRORS!----------");
    window.venus_main = window.venus;
    window.driver = venus_main.venus.Driver;
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

}

function local_riscv() {
    loadScript("js/risc-mode.js", "alert('COULD NOT LOAD RISCVMODE SCRIPT!');", "local_kotlin();");
}

function local_kotlin() {
    // loadScript("https://try.kotlinlang.org/static/kotlin/1.3.11/kotlin.js", "alert('COULD NOT LOAD KOTLIN SCRIPT!');", "local_venus();");
    loadScript("../../../build/kotlin-js-min/main/kotlin.js", "alert('COULD NOT LOAD KOTLIN SCRIPT!');", "local_venus();");
}

function local_venus() {
    if (typeof kotlin.kotlin.Number === "undefined") {
        kotlin.kotlin.Number = function (){}
        kotlin.kotlin.Number.prototype.call = function(a){}
    }
    loadScript("../../../build/kotlin-js-min/main/venus.js", "alert('COULD NOT LOAD VENUS SCRIPT!');", "setup_venus();")
}

function main_venus() {
    loadScript("venus.js", "local_riscv();", "setup_venus();");
}

main_venus();