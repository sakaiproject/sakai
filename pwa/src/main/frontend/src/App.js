// export default App;
import { setupIonicReact } from "@ionic/react";

import { BrowserRouter, Routes, Route } from "react-router-dom"

/* Core CSS required for Ionic components to work properly */
import "@ionic/react/css/core.css";

/* Basic CSS for apps built with Ionic */
import "@ionic/react/css/normalize.css";
import "@ionic/react/css/structure.css";
import "@ionic/react/css/typography.css";

/* Optional CSS utils that can be commented out */
import "@ionic/react/css/padding.css";
import "@ionic/react/css/float-elements.css";
import "@ionic/react/css/text-alignment.css";
import "@ionic/react/css/text-transformation.css";
import "@ionic/react/css/flex-utils.css";
import "@ionic/react/css/display.css";

/* Theme variables */
import Create from "./pages/Create/Create";
import Login from "./pages/Login/Login";
import Register from "./pages/Register/Register";
import List from "./pages/List/List";
import { useEffect, useState } from "react";
import { io } from "socket.io-client";
import { NOTIFICATION_HOST } from "./constants";

setupIonicReact();

const App = () => {

  const [socket, setSocket] = useState(null);
  // establish socket connection to socket server
  useEffect(() => setSocket(io(NOTIFICATION_HOST)), []);

  useEffect(() => {

    const currentUser = JSON.parse(localStorage.getItem("user"));
    currentUser && socket && socket.emit("newUser", { user: currentUser });
  }, [socket]);

  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/pwa/" element={<List socket={socket} />} />
          <Route path="/pwa/login" element={<Login />} />
          <Route path="/pwa/register" element={<Register />} />
          <Route path="/pwa/create" element={<Create socket={socket} />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
};

export default App;
