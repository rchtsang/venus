/*
    Created by Stephan Kaminsky
*/
console.log("Loading decoder interface...");
var debug = false;
if (typeof decoder === "undefined") {
  decoder = {};
}
function CopyToClipboard(containerid) {
  var copyText = document.getElementById(containerid);

  /* Select the text field */
  copyText.select();

  /* Copy the text inside the text field */
  document.execCommand("Copy");
  return
}
function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}
function openDecoder() {
  venus_main.venus.Renderer.renderTab("decoder", venus_main.venus.Renderer.mainTabs)
}
function closeDecoder() {
  var decodertab = document.getElementById("decoder-tab");
  var decodertv = document.getElementById("decoder-tab-view");
  if (decodertv !== null) {
    decodertv.style.display = "none";
  }
  if (decodertab !== null) {
    decodertab.setAttribute("class", "");
  }
}
function toggleThis(e) {
  if (e.value == "true") {
    e.classList.remove("is-primary");
    e.value = "false";
  } else {
    e.classList.add("is-primary");
    e.value = "true";
  }
}
function validateBase(e) {
  if (e.value == "") {
    return;
  }
  if (e.value < 1) {
    e.value = 2;
  }
  if (e.value > 32) {
    e.value = 32;
  }
}
function downloadDecoded(id, filename, custom_name) {
  var cusN = prompt("Please enter a name for the file. Leave blank for default.");
  if (cusN != "") {
    if (cusN == null) {
      return;
    }
     if (cusN.split('.').pop() != filename.split('.').pop()) {
      cusN = cusN + "." + filename.split('.').pop();
    }
    filename = cusN;
  }
  var text = document.getElementById(id).value;
  var element = document.createElement('a');
  element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
  element.setAttribute('download', filename);

  element.style.display = 'none';
  document.body.appendChild(element);

  element.click();

  document.body.removeChild(element);
}
function setAlert(m) {
  document.getElementById("alerts").innerHTML = m;
}

function decode() {
    if (typeof decoder !== "object") {
      var s = "[ERROR]: Could not find the decoder object! Check to see if the decoder actually loaded!";
      alert(s);
      console.warn(s);
      return;
    }
    var reg = /0x.*/i;
    var decodebut = document.getElementById("decoder-decode");
    decoder.useSudoRegs = document.getElementById("sudoRegID").value == "true";
    decoder.pseudoDecode = document.getElementById("usePseudoInst").value == "true";
    decoder.nopAll = document.getElementById("nopAll").value == "true";
    decoder.addLabels = document.getElementById("addLabels").value == "true";
    decodebut.classList.add("is-loading");
    try {
      //This function will go line by line and attempt to decode instrucitons.
    program = document.getElementById("hex-inst").value;
    decoded = [];
    instructions = [];
    if (program) {
      program = program.split(/\n|\s/g);
      for (line of program) {
        if (!reg.test(line) && line != "") {
          line = "0x" + line;
        }
        instructions.push(new Instruction(line));
        decoder.multiPseudo(instructions);
      }
      decoder.labeler(instructions);
      for (i of instructions) {
        decoded.push(i.decode());
      }
    }
    decoded.push("");
    document.getElementById("decoded-inst").value = decoded.join("\n");
    } catch (e) {
      document.getElementById("decoded-inst").value = "An error has occured! Please view the console to see the error!";
      throw e;
    }
    decodebut.classList.remove("is-loading");
}

function clearDecoder() {
  document.getElementById("decoded-inst").value = "";
  document.getElementById("hex-inst").value = "";
}

function venusdecoder() {
  var lielem = document.createElement('li');
  var aelem = document.createElement('a');
  //aelem.setAttribute("onclick", "genTraceMain()");
  aelem.setAttribute("onclick", "openDecoder()");
  lielem.setAttribute("id", "decoder-tab");
  var secelem = document.createElement('section');
  secelem.setAttribute("class", "section");
  secelem.setAttribute("id", "decoder-tab-view");
  secelem.style.display = "none";
  secelem.innerHTML = `<center><div class="tile is-ancestor">
  <div class="tile is-vertical">
    <div class="tile">
      <div class="tile is-parent">
          <article class="tile is-child is-primary" align="center">
            <font size="6px">RISC-V Disassembler v1.0.1</font><br>
            <font size="4px">Created by <b>Stephan Kaminsky</b></font><br>
            <font size="2px">Please note that this is an older script which I have not maintained in a while. It only works with most 32b instructions but may not work on all of the instructions which the main venus works with. You can also decode instructions by putting the hex instruction into the editor and assembling that.</b></font>
          </article>
        </center>
      </div>
    </div>
    <div class="tile">
      <div class="tile is-parent">
        <article class="tile is-child is-primary" id="simulator-controls-container">
          <div class="field is-grouped is-grouped-centered">
            <div class="control">
              <button id="decoder-decode" class="button is-primary" onclick="decode()">Disassemble</button>
            </div>
            <div class="control">
              <button id="decoder-clear" class="button" onclick="clearDecoder()">Clear</button>
            </div>
           </div>
         </article>
       </div>
     </div>
     <div class="tile">
       <div class="tile is-parent">
         <article class="tile is-child is-primary" align="center">
            <center>
            Options!
            <table id="options" class="table" style="width:50%; margin-bottom: 0;">
              <thead>
                <tr>
                  <th><center>Use Registers Pseudo-names</center></th>
                  <th><center>Add labels</center></th>
                  <th><center>Decode to Pseudo Instruction<br>if possible (experimental)</center></th>
                  <th id="nop" style="display:none;"><center>Convert any line which<br>does nothing to a nop</center></th>
                </tr>
              </thead>
                <tr>
                  <th><center><button id="sudoRegID" class="button is-primary" onclick="toggleThis(this)" value="true">Pseudo RegID</button></center></th>
                  <th><center><button id="addLabels" class="button is-primary" onclick="toggleThis(this)" value="true">Add Labels</button></center></th>
                  <th><center><button id="usePseudoInst" class="button" onclick="toggleThis(this); nopVisiblity(this.value);" value="false" >Attempt Pseudo Decode</button></center></th>
                  <th><center><button id="nopAll" style="display:none;" class="button" onclick="toggleThis(this)" value="false">nop All</button></center></th>
                </tr>
            </table><br>
            </center>
         </article>
       </div>
     </div>
     <div class="tile is-parent">
      <article class="tile is-child">
        Instruction Hex:&nbsp;&nbsp;&nbsp;&nbsp;<a onclick="CopyToClipboard('hex-inst')">Copy!</a>
        <br>
        <font size="2px">Make sure your instructions are in the correct hex format: Ex. 0xFFFFFFFF or FFFFFFFF. Only put instruction separated by spaces or new lines! Unknown instruction will not be decoded.</font>
        <br>
        <textarea id="hex-inst" class="textarea" placeholder="Input Instruction Hex" style="height: 250px;"></textarea>
      </article>
    </div>
    <div class="tile is-parent">
      <article class="tile is-child">
        Disassembled Instructions:&nbsp;&nbsp;&nbsp;&nbsp;<a onclick="CopyToClipboard('decoded-inst')">Copy!</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onclick="downloadtrace('decoded-inst', 'decoded.hex', true)">Download!</a>&nbsp;&nbsp;&nbsp;&nbsp;<a onclick="loadToEditor();">Load to Editor!</a>
        <br>
        <textarea id="decoded-inst" class="textarea" placeholder="trace dump output" style="height: 250px;" readonly=""></textarea>
      </article>
    </div>
    <br><br>
   </div>
  </div></center>`;
  aelem.innerHTML = "Decoder";
  lielem.appendChild(aelem);
  document.getElementsByClassName('tabs')[0].children[0].appendChild(lielem);
  insertAfter(secelem, document.getElementById("simulator-tab-view"));  

  var noticelm = document.createElement("div");
  noticelm.setAttribute("id", "alertsDiv");
  noticelm.innerHTML = `
    <center>
      <div id="alerts">
      </div>
    </center>
  `;
  document.body.insertBefore(noticelm, document.body.children[0]);
  venus_main.venus.Renderer.addTab("decoder", venus_main.venus.Renderer.mainTabs);
}

function nopVisiblity(vis){
  var des = document.getElementById("nop");
  var button = document.getElementById("nopAll");
  var v = "none";
  if (vis == "true") {
    v = ""; 
  }
  des.style.display = v;
  button.style.display = v;
}

function addTabs(text) {
  var tab = RegExp("\\t", "g");
  text.replace(tab,'\t');
}

function loadToEditor() {
  val = document.getElementById("decoded-inst").value;
  if (codeMirror.getValue()) {
    if (!confirm("There was some code already in your editor! Do you want to override that code?")) {
      return;
    }
  }
  driver.openEditor();
  codeMirror.setValue(val);
  codeMirror.refresh();
}

var curNumBase = 2;
function removeDecoder() {
  venus_main.venus.Renderer.removeTab("decoder", venus_main.venus.Renderer.mainTabs);
  if (document.getElementById("decoder-tab").classList.contains("is-active")) {
       venus_main.venus.Renderer.renderTab("editor", venus_main.venus.Renderer.mainTabs);
  }
  dcdrval = document.getElementById("hex-inst").value;
  document.getElementById("decoder-tab").remove();
  document.getElementById("alertsDiv").remove();
  document.getElementById("decoder-tab-view").remove();
  window.decoderIsLoaded = undefined;
  return;
  if (typeof driver.tos === "undefined") {
      driver.openSimulator = driver.os;
  } else {
      driver.openSimulator = driver.tos;
  }
  
}

function mainDecoder() {

  if (typeof decoderIsLoaded == "undefined") {
    console.log("Loading decoder...");
     venusdecoder();
  } else {
    console.log("Reloading decoder...");
    var decodertab = document.getElementById("decoder-tab");
    var wasactive = false;
    if (decodertab && decodertab.getAttribute("class") == "is-active") {
      wasactive = true;
    }
    removeDecoder();
    venusdecoder();
    if (typeof dcdrval != "undefined") {
      document.getElementById("hex-inst").value = dcdrval;
    }
    if (wasactive) {
      openDecoder();
    }
  }
  
}
mainDecoder();
var decoderIsLoaded = true;
console.log("Decoder interface loaded!");