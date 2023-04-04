//////////////////////////////////////////////////////////////
// CARD GAME API                                            //
// Fetch functions to interface with the card-game REST API //
//////////////////////////////////////////////////////////////

// Persists the result of the check - a hit or miss
export async function fetchCheckResult(siteId, userId, correct) {
    if (typeof siteId !== "string" || typeof userId !== "string" || typeof correct !== "boolean") {
        console.error(`Passed value siteId [${siteId}], userId [${userId}] or correct [${correct}] is invalid.`);
        return false;
    }

    const response = await fetch(`/api/sites/${siteId}/card-game/users/${userId}/checkResult?correct=${correct}`, {
        method: "PUT",
        body: { correct }
    });

    if (!response.ok) {
        console.error("Could not fetch check result");
    }

    return response.ok;
}

// Gets the game config
export async function fetchConfig(siteId) {
    if (typeof siteId !== "string") {
        console.error(`Passed value siteId [${siteId}] is invalid.`);
        return false;
    }

    const response = await fetch(`/api/sites/${siteId}/card-game/config`);

    if (response.ok) {
        return await response.json();
    } else {
        console.error("Config could not be fetched:", response.statusText);
        return null;
    }
}

// Performs a reset - this will delete all stats for the users of this site
export async function fetchReset(siteId) {
    if (typeof siteId !== "string") {
        console.error(`Passed value siteId [${siteId}] is invalid.`);
        return false;
    }

    const response = await fetch(`/api/sites/${siteId}/card-game/reset`, { method: "DELETE" });

    if (!response.ok) {
        console.error("Could not fetch game reset");
    }

    return response.ok;
}

// Gers all user data of users, that will display in the game
export async function fetchUsers(siteId) {
    if (typeof siteId !== "string") {
        console.error(`Passed value siteId [${siteId}] is invalid.`);
        return false;
    }

    const response = await fetch(`/api/sites/${siteId}/card-game/users`);

    if (response.ok) {
        return await response.json();
    } else {
        console.error("Users could not be fetched:", response.statusText);
        return null;
    }
}

export default {
    fetchCheckResult,
    fetchConfig,
    fetchReset,
    fetchUsers,
};
