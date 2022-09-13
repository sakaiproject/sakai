import {
  IonContent,
  IonHeader,
  IonList,
  IonPage,
  IonTitle,
  IonToolbar,
  IonIcon,
  IonButton,
  IonInput,
  IonItem,
  IonLabel,
  IonToggle,
  IonTextarea,
  IonFabButton,
} from "@ionic/react";
import "./Create.css";
import { home } from "ionicons/icons";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { openDB } from "idb";
import { BACKEND_HOST } from "../../constants";

const Create = ({ socket }) => {

  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user"));
  if (user === null) navigate("/pwa");

  const homeBtnClickHandler = () => navigate("/pwa");

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [isPrivate, setIsChecked] = useState(true);

  const putToIndexDB = async (newNote) => {

    const BASE_NAME = 'backgroundSync';
    const STORE_NAME = 'messages';
    const VERSION = 1;
    const db = await openDB(BASE_NAME, VERSION);
    db.add(STORE_NAME, newNote, newNote._id);
    db.close();
  }

  const handleCreate = async () => {

    const newNote = {
      title: title,
      content: content,
      userId: user.uid,
      isPrivate: isPrivate,
      _id: new Date().getTime()
    };
    try {
      const url = BACKEND_HOST + (isPrivate ? "/notes" : "/public");
      const res = await fetch(url, {
        method: "POST",
        body: JSON.stringify(newNote),
        headers: { "Content-type": "application/json; charset=UTF-8" },
      });
      console.log(res);
      if (!isPrivate) {
        socket.emit("newPublicPost", { sender: JSON.parse(localStorage.getItem("user")).email });
      }

      navigate("/pwa");
    } catch (error) {
      if (!window.navigator.onLine) {
        putToIndexDB(newNote);
        const sw = await window.navigator.serviceWorker.ready;
        await sw.sync.register('back-sync');
        navigate('/pwa');
      }
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar color="primary">
          <IonTitle>New</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent class="ion-padding">
        <IonList>
          <IonItem>
            <IonLabel color="dark">
              <h2>{isPrivate ? "Private" : "Public"}</h2>
            </IonLabel>
            <IonToggle
                checked={isPrivate}
                onIonChange={e => {
                    setIsChecked(e.detail.checked)
                }}
            />
          </IonItem>
          <IonItem>
            <IonLabel color="dark">
              <h2>Title</h2>
            </IonLabel>
            <IonInput
                value={title}
                onIonChange={(e) => setTitle(e.target.value)}
            ></IonInput>
          </IonItem>
          <IonItem>
            <IonLabel>
              <h2>Content</h2>
            </IonLabel>
            <IonTextarea
              autoGrow={true}
              onIonChange={(e) => setContent(e.target.value)}
            ></IonTextarea>
          </IonItem>
        </IonList>

        <div className="create-btn-container">
          <IonButton onClick={handleCreate}>Create</IonButton>
        </div>

        <div className="home-btn-container">
          <IonFabButton onClick={homeBtnClickHandler}>
            <IonIcon icon={home}></IonIcon>
          </IonFabButton>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default Create;
