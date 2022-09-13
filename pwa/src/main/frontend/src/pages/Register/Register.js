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
  useIonAlert,
} from "@ionic/react";
import "../Login/Login.css";
import { keyOutline, mailOutline } from "ionicons/icons";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { createUserWithEmailAndPassword } from "firebase/auth";
import { auth } from "../../firebase";

const Register = () => {
  const navigate = useNavigate();
  const [present] = useIonAlert();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const handleRegister = () => {

    if (email.trim().length === 0) {
      present({
        header: "Error",
        message: "email cannot be empty",
        buttons: [{ text: "Ok", handler: (d) => console.log("ok pressed") }],
        onDidDismiss: (e) => console.log("did dismiss"),
      });
    } else if (password !== confirmPassword) {
      present({
        header: "Error",
        message: "passwords must be the same",
        buttons: [{ text: "Ok", handler: (d) => console.log("ok pressed") }],
        onDidDismiss: (e) => console.log("did dismiss"),
      });
    } else {
      createUserWithEmailAndPassword(auth, email, password).then(({ user }) => {
        localStorage.setItem("user", JSON.stringify(user));
        console.log(user);
        navigate("/pwa");
      });
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar color="primary">
          <IonTitle>PWA Quick Notes</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent class="ion-padding ion-center">
        <IonList>
          <IonItem>
            <IonLabel position="floating" color="dark">
              <IonIcon icon={mailOutline} size="large"></IonIcon>
            </IonLabel>
            <IonInput
              placeholder="email"
              value={email}
              onIonChange={(e) => setEmail(e.target.value)}
            ></IonInput>
          </IonItem>
          <IonItem>
            <IonLabel position="floating">
              <IonIcon icon={keyOutline} size="large"></IonIcon>
            </IonLabel>
            <IonInput
              placeholder="password"
              type="password"
              value={password}
              onIonChange={(e) => setPassword(e.target.value)}
            ></IonInput>
          </IonItem>
          <IonItem>
            <IonLabel position="floating">
              <IonIcon icon={keyOutline} size="large"></IonIcon>
            </IonLabel>
            <IonInput
              placeholder="confirm password"
              type="password"
              value={confirmPassword}
              onIonChange={(e) => setConfirmPassword(e.target.value)}
            ></IonInput>
          </IonItem>
        </IonList>

        <div className="login-btn-container">
          <IonButton onClick={handleRegister}>Register</IonButton>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default Register;
