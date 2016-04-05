function privateMessage(playerSlot, playerName){
    sweetPrompt("Please enter your message", "for "+ playerName , "Send!", "Cancel", function(response) {
        $.ajax(jsRoutes.controllers.Rcon.privateMessage(playerSlot, response));
    });
}

function punishOnline(action, notification) {
    $.ajax(action);
    $.notify(notification,
        {
            position: 'bottom center',
            className: "success"
        });
}