/////////////////////////////////////////////////////////
// CARD GAME INDEX                                     //
// Exports loadCardGame, which initiates the card-game //
/////////////////////////////////////////////////////////

import CardGame from "./card-game.js";
import { fetchConfig, fetchUsers } from "./card-game-api.js";
import { loadProperties } from "/webcomponents/sakai-i18n.js";

function showError(appId, message) {
    console.error(message);

    const root = document.getElementById(appId);
    if (root) {
        root.innerHTML = `<div class="sak-banner-error">${message}</div>`;
    }
}

async function fetchI18nData() {
    const i18n = await loadProperties({ bundle: "card_game" });

    if (i18n?.game_name) {
        return i18n;
    } else {
        console.error("Could not get i18n properties for card game.");
        return null;
    }
}

export default async function loadCardGame(appId, siteId) {
    const [i18n, users, config] = await Promise.all([fetchI18nData(), fetchUsers(siteId), fetchConfig(siteId)]);

    if (!i18n) {
        showError(appId, "Could not load internationalization data for the Game");
        return;
    }

    if (users && config) {
        const cardGame = new CardGame(appId, i18n, config, { siteId, users });
        cardGame.start();
    } else {
        showError(appId, i18n["error_loading"]?.replace("{0}", i18n["game_name"]));
    }
}
