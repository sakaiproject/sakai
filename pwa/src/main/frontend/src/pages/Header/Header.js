import React from 'react'
import {
  IonHeader,
  IonTitle,
  IonToolbar,
  IonIcon,
  IonBadge,
  IonItem,
  IonButton,
} from "@ionic/react";
import { paperPlane, notifications, logOut } from "ionicons/icons";
import "./Header.css"
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Header = ({ socket }) => {

  const [isOpen, setIsOpen] = useState(false);
  const [notificationQueue, setNotificationQueue] = useState([]);
  const navigate = useNavigate();

  const currentUser = JSON.parse(localStorage.getItem("user"));
  if (!currentUser) navigate('/pwa/login');

  const broadcastClickHandler = () => {
    socket.emit("sendNotification", { sender: currentUser.email });
  }

  const handleLogout = () => {

    localStorage.removeItem("user");
    navigate("/pwa/login");
  }

  useEffect(() => {
    socket && socket.on("getNotification", (notification) => {
      console.log(notification);
      setNotificationQueue(prev => [...prev, notification]);
    })
  }, [socket]);

  return (
    <IonHeader>
      <IonToolbar color="primary" className="nav-bar">
        <IonTitle>Notes</IonTitle>
        <IonIcon icon={paperPlane} id="send-notification" className="megaphone-icon" onClick={broadcastClickHandler} />
        <IonIcon icon={notifications} className="notifications-icon" onClick={() => setIsOpen(prev => !prev)} />
        {
          notificationQueue.length === 0 ? null :
              <IonBadge color="danger" className="notification-badge">{notificationQueue.length}</IonBadge>
        }
        <IonIcon icon={logOut} className="logout-icon" onClick={handleLogout} />
      </IonToolbar>
      {
        isOpen && notificationQueue.length > 0 &&
        (<div className="notification-list">
          {
            notificationQueue.map((n, idx) =>
              <IonItem key={idx} >{n.sender}</IonItem>
            )
          }
          <IonButton className="clear-btn" onClick={() => {
              setIsOpen(false);
              setNotificationQueue([])
          }} >CLEAR</IonButton>
        </div>)
      }

    </IonHeader>
  );
};

export default Header;
