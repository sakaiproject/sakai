/////////////////////////////////////////
// CARD GAME UTILS                     //
// Utility functions for the card-game //
/////////////////////////////////////////

// Calculates weather a given user can be considered learned
export function isUserLearned(user, config) {
    const { hits, misses, markedAsLearned } = user;
    const { minAttempts, minHitRatio } = config;

    const attempts = hits + misses;
    const hitRatio = hits / attempts;

    return markedAsLearned || attempts >= minAttempts && hitRatio >= minHitRatio;
}

// Calculates the weight that influences the likelihood of the user being rolled
export function calculateUserWeight(user, currentUserId) {
    const baseWeight = 0;
    const currentUserDiscount = user.id === currentUserId ? 100 : 0;

    return baseWeight - currentUserDiscount;
}

// Picks a random user respecting the calculated weight
export function rollUser(users, currentUserId) {
    if (!users || users.length === 0) {
        return null;
    }

    const weights = users.map((user) => calculateUserWeight(user, currentUserId));
    const lowestWeight = weights.slice().sort()[0];
    const zeroBasedWeights = weights.slice().map((weight) => weight - lowestWeight);

    return randomUser(users, zeroBasedWeights).id;
}

// Plays a sound file found on provided path
export async function playSound(path) {
    const audio = new Audio(path);
    audio.play();
}

// Inspired by:
// https://stackoverflow.com/questions/43566019/how-to-choose-a-weighted-random-array-element-in-javascript#answer-55671924
function randomUser(users, userWeights) {
    const weights = userWeights.slice();

    // If all weights equal, return random user ignoring weights
    if (weights.every((weight) => weight === weights[0])) {
        const random = Math.floor(Math.random() * users.length);
        return users[random];
    }

    for (let i = 1; i < weights.length; i++) {
        weights[i] += weights[i - 1];
    }

    const random = Math.random() * weights[weights.length - 1];
    let count = 0;
    for (; count < weights.length; count++) {
        if (weights[count] > random) {
            break;
        }
    }

    return users[count];
}
