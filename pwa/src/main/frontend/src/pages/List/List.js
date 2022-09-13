import {
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardTitle,
  IonContent,
  IonList,
  IonPage,
  IonIcon,
  IonFabButton,
  IonFab,
  IonTitle,
  IonItemDivider
} from "@ionic/react";
import "./List.css";
import { addOutline, closeCircle, refresh } from "ionicons/icons";
import { useNavigate } from "react-router-dom";
import { Fragment, useEffect, useState } from "react";
import { openDB } from 'idb';
import { BACKEND_HOST } from "../../constants";
import PublicPost from "../PublicPost/PublicPost";
import Header from "../Header/Header";


const List = ({ socket }) => {

  const navigate = useNavigate();
  const [notes, setNotes] = useState([]);
  const [mode, setMode] = useState('online');

  useEffect(() => {
    console.log("we have access to the internet!");
    setMode("online")
  }, [socket])

  const createIndexDB = async () => {
    const BASE_NAME = 'backgroundSync';
    const STORE_NAME = 'messages';
    const VERSION = 1;

    // create database and store
    openDB(BASE_NAME, VERSION, {
      upgrade(db) {
        db.createObjectStore(STORE_NAME);
      },
    });
  }

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (!user) {
      navigate("/pwa/login");
      return
    }

    createIndexDB();
    fetch(`${BACKEND_HOST}/notes/${user.uid}`)
      .then(reponse => {
        reponse.json()
          .then(result => {
            localStorage.setItem("data", JSON.stringify(result));
            setNotes(result);
            setMode("online")
          })
      })
      .catch(err => {
        if (!window.navigator.onLine) {
          let collection = localStorage.getItem("data");
          setNotes(JSON.parse(collection));
          setMode('offline');
        }
      })
  }, []);


  const btnClickHandler = () => {
    navigate("/pwa/create");
  };

  const handleRefresh = () => {
    window.location.reload();
  }

  const btnRemoveHandler = async (noteId) => {
    console.log(noteId);
    const response = await fetch(`${BACKEND_HOST}/notes/${noteId}`, {
      method: 'DELETE', headers: {
        "Content-type": "application/json; charset=UTF-8"
      }
    })
    const deletedNote = await response.json();

    console.log(deletedNote);
    setNotes(notes.filter(note => note._id !== deletedNote._id))
  };

  return (
    <Fragment>
      <Header socket={socket} />
      <IonPage className="list-container">
        <div>
          {
            mode === 'offline' ? <div className="alertMessageContainer">
              <div className="offline-notification">
                <h1>You are in offline mode, click for refresh</h1>
                <IonIcon className="refresh-icon" icon={refresh} onClick={handleRefresh}></IonIcon>
              </div>
            </div> : null
          }
        </div>
        {
          <IonContent class="ion-padding">
            <IonTitle>Personal Post</IonTitle>
            <IonList>
              {notes.map((note) => (
                <IonCard key={note._id}>
                  <IonCardHeader>
                    <IonFab vertical="top" horizontal="end">
                      <IonFabButton
                        color="danger"
                        onClick={() => btnRemoveHandler(note._id)}
                        className="close-btn">
                        <IonIcon icon={closeCircle}></IonIcon>
                      </IonFabButton>
                    </IonFab>
                    <IonCardTitle>{note.title}</IonCardTitle>
                  </IonCardHeader>
                  <IonCardContent>{note.content}</IonCardContent>
                </IonCard>
              ))}
            </IonList>
            <IonItemDivider />
            <PublicPost socket={socket} />
            <div className="add-btn-container" >
              <IonFab slot="fixed">
                <IonFabButton onClick={btnClickHandler}>
                  <IonIcon icon={addOutline}></IonIcon>
                </IonFabButton>
              </IonFab>
            </div>
          </IonContent>
        }
      </ IonPage>
    </Fragment>

  );
};

export default List;
