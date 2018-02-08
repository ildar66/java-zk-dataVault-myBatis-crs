var widgets = []
var editor

function apply_wysiwyg (id) {
    try{
    editor = CodeMirror.fromTextArea(document.getElementById(id), {
        lineNumbers: true,
        extraKeys: {"Ctrl-Space": "autocomplete"},
        mode: {name: "javascript", globalVars: true}
    });

    window.editor = editor
    var waiting;
        editor.on("change", function() {
            clearTimeout(waiting);
            waiting = setTimeout(updateHints, 500);
        });

        setTimeout(updateHints, 100);
    } catch(e) {
        alert(e);
    }
}

function submit_code (widgetId) {
    zAu.send(new zk.Event(zk.Widget.$('$'+widgetId), 'onSubmit', editor.getValue()));
}

function updateHints() {
   editor.operation(function(){
       return;
        for (var i = 0; i < widgets.length; ++i)
            editor.removeLineWidget(widgets[i]);
        widgets.length = 0;

        JSHINT(editor.getValue());
        for (var i = 0; i < JSHINT.errors.length; ++i) {
            var err = JSHINT.errors[i];
            if (!err) continue;
            var msg = document.createElement("div");
            var icon = msg.appendChild(document.createElement("span"));
            icon.innerHTML = "!!";
            icon.className = "lint-error-icon";
            msg.appendChild(document.createTextNode(err.reason));
            msg.className = "lint-error";
            widgets.push(editor.addLineWidget(err.line - 1, msg, {coverGutter: false, noHScroll: true}));
        }
    });
    var info = editor.getScrollInfo();
    var after = editor.charCoords({line: editor.getCursor().line + 1, ch: 0}, "local").top;
    if (info.top + info.clientHeight < after)
        editor.scrollTo(null, after - info.clientHeight + 3);

    zAu.send(new zk.Event(zk.Widget.$('$ww_code'), 'onUpdate', editor.getValue()));
}

//Функции, не относящиеся к редактору

function appendFormula (formula) {
    editor.setValue(editor.getValue()+formula);
}