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
  IonText,
} from "@ionic/react";
import "./Login.css";
import { keyOutline, mailOutline } from "ionicons/icons";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { auth } from "../../firebase";
import { signInWithEmailAndPassword } from "firebase/auth";

const Login = () => {

  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errMessage, setErrMessage] = useState("");

  const handleRegister = e => {

    e.preventDefault();
    navigate("/pwa/register");
  };

  const handleLogin = () => {

    signInWithEmailAndPassword(auth, email, password)
      .then(response => {

        localStorage.setItem("user", JSON.stringify(response.user));
        setErrMessage("");
        navigate("/pwa");
      })
      .catch (err => setErrMessage(err));
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
                class="input-area"
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
          {errMessage && (
            <div className="err-message-container">
              <IonText>Wrong email or password</IonText>
            </div>
          )}
        </IonList>

          <div className="login-btn-container">
            <IonButton onClick={handleLogin}>Login</IonButton>
          </div>
          <div className="register-link-container">
            New User? <a href="javascript:;" onClick={handleRegister}>Register</a>
          </div>
      </IonContent>
    </IonPage>
  );
};

export default Login;
