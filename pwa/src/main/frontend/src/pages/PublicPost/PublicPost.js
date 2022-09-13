import { IonCard, IonCardContent, IonCardHeader, IonCardTitle, IonFab, IonFabButton, IonIcon, IonList, IonTitle } from '@ionic/react'
import { closeCircle } from "ionicons/icons";
import React from 'react'
import { useState } from 'react'
import { useEffect } from 'react'
import { BACKEND_HOST } from '../../constants'

import './PublicPost.css'
const PublicPost = ({ socket }) => {

  const [notes, setNotes] = useState([]);
  const currentUser = JSON.parse(localStorage.getItem("user"));

  const btnRemoveHandler = async (noteId) => {

    const response = await fetch(`${BACKEND_HOST}/public/${noteId}`, {
      method: 'DELETE',
      headers: { "Content-type": "application/json; charset=UTF-8" },
    });
    const deletedNote = await response.json();
    socket.emit("newPublicPost", { sender: currentUser.email });
    setNotes(notes.filter(note => note._id !== deletedNote._id));
  };

  useEffect(() => {

    fetch(`${BACKEND_HOST}/public/`).then(res => {

      res.json().then(data => {
        console.log('public', data);
        localStorage.setItem("public", JSON.stringify(data));
        setNotes(data);
      });
    }).catch (err => {
        if (!window.navigator.onLine) {
          let collection = localStorage.getItem("public");
          console.log("collection: ", collection);
          setNotes(JSON.parse(collection));
        }
    });
  }, [])

  useEffect(() => {

      socket && socket.on("getPublicPost", creator => {
        fetch(`${BACKEND_HOST}/public/`).then(
          res => {
            res.json().then(data => {
              console.log('public', data);
              localStorage.setItem("public", JSON.stringify(data));
              setNotes(data);
            });
          });
      });
  }, [socket]);

  return (
    <div className='public-post-container'>
      <IonTitle className='public-post-title'>PUBLIC</IonTitle>
      <IonList>
        {
          notes.map(note => (
            <IonCard key={note._id}>
              <IonCardHeader>
                {
                  currentUser.uid === note.userId ? (
                    <IonFab vertical="top" horizontal="end">
                      <IonFabButton
                          color="danger"
                          onClick={() => btnRemoveHandler(note._id)}
                          className="close-btn">
                          <IonIcon icon={closeCircle}></IonIcon>
                      </IonFabButton>
                    </IonFab>
                  ) : null
                }
                <IonCardTitle>
                    {note.title}
                </IonCardTitle>
              </IonCardHeader>
              <IonCardContent>
                  {note.content}
              </IonCardContent>
            </IonCard>
          ))
        }
      </IonList>
    </div>
  );
};

export default PublicPost
