var stompClient = null;


function connect() {
  var socket = new SockJS('/ws');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe("/user/233/queue/messages", function (greeting) {
      console.log('greeting', JSON.parse(greeting.body))
    });
  });
}


setTimeout(connect, 1000)


const sendMessage = (msg) => {
    const message = {
      senderId: '',
      recipientId: 233,
      senderName: '',
      recipientName: '',
      content: msg,
      timestamp: new Date(),
    };
    stompClient.send("/app/chat", {}, JSON.stringify(message));


};

setInterval(()=>{sendMessage('233')}, 2000)
