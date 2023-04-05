/////////////////////////////////////
// CARD GAME                       //
// Implementation of the card-game //
/////////////////////////////////////

import BaseGame from "./base-game.js";
import { isUserLearned, rollUser, playSound } from "./card-game-utils.js";
import { fetchCheckResult, fetchReset, fetchMarkAsLearned } from "./card-game-api.js";

const VIEWS = {
    GAME: "VIEW_GAME",
    GAME_OVER: "VIEW_GAME_OVER",
    NO_STUDENTS: "VIEW_NO_STUDENTS"
};

const FEEDBACK_STATES = {
    NO: "HIDDEN",
    OK: "CORRECT",
    KO: "WRONG"
};

const SOUNDS = {
    HIT: "/sakai-roster2-tool/js/card-game/sounds/hit-sound.mp3",
    MISS: "/sakai-roster2-tool/js/card-game/sounds/miss-sound.mp3",
}

export default class CardGame extends BaseGame {

    constructor(appId, i18n, config, { siteId, users }) {
        super(appId, i18n, config);

        this.checkConfig("minAttempts");
        this.checkConfig("minHitRatio");
        this.checkConfig("showOfficialPhoto");

        this.siteId = siteId;

        this.selectId = "user-name";

        this.initState({ allUsers: users , soundEnabled: true});
    }

    initState(data) {
        this.state = {
            ...data,
            feedbackState: FEEDBACK_STATES.NO,
            official: this.config.showOfficialPhoto ?? false,
        };
    }

    //Override
    render() {
        switch(this.state.view) {
            case VIEWS.GAME:
                return this.renderGame();
            case VIEWS.GAME_OVER:
                return this.renderGameOver();
            case VIEWS.NO_STUDENTS:
                return this.renderNoStudentsInfo();
            default:
                console.log("Unknown view", this.state.view);
                return "";
        }
    }

    //Override
    updateCalcState(init) {
        super.updateCalcState();
        this.state.learnUsers = this.calcLearnUsers();
        this.state.view = this.calcView();
        if (init || this.rollUser) {
            this.state.currentUserId = this.getNewStudentId();
            this.rollUser = false;
        }
        this.state.currentUser = this.calcCurrentUser();
        this.state.previousUser = this.calcPreviousUser();
        this.state.userOptionsData = this.calcUserOptionsData();
        this.state.progress = this.calcProgress();
    }

    //Override
    updateHandlers() {
        super.updateHandlers();
        switch(this.state.view) {
            case VIEWS.GAME:
                document.querySelector("[data-check]").addEventListener("click",
                        (event) => this.checkName());
                document.querySelector("[data-reroll]").addEventListener("click",
                        (event) => {
                            this.mutateRerollUser();
                            this.effectRemoveFeedbackBanner();
                        });
                document.querySelector("[data-mute]").addEventListener("click",
                        (event) => {
                            this.mutateMute();
                            this.effectRemoveFeedbackBanner();
                        });
                document.querySelector("[data-reset]").addEventListener("click",
                        (event) => this.resetGame());
                document.querySelector("[data-mark-learned]")?.addEventListener("click",
                        (event) => {
                            this.markAsLearned();
                            this.mutateRerollUser();
                            this.effectRemoveFeedbackBanner();
                        });

                // Unfortunately we can't listen to jQuery internal events without jQuery
                const select = document.getElementById(this.selectId);
                $(select).on("select2:opening", (event) => this.effectRemoveFeedbackBanner());
                $(select).on("select2:select", (event) => this.effectEnableCheckButton());
                break;
            case VIEWS.GAME_OVER:
                document.querySelector("[data-reset]").addEventListener("click",
                        (event) => this.resetGame());
                break;
            default:
                break;
        }
    }

    //Override
    updated() {
        super.updated();
        this.effectInitSelect();
    }

    // Resets the game - set all hits and misses to 0 and start again
    resetGame() {
        fetchReset(this.siteId);

        const users = this.state.allUsers.map((user) => {
            return {
                ...user,
                hits: 0,
                misses: 0,
                markedAsLearned: false,
            }
        });

        this.initState({ allUsers: users });

        super.start();
    }

    markAsLearned() {
        const userId = this.state.previousUser?.id;

        if (userId) {
            fetchMarkAsLearned(this.siteId, userId);

            this.mutateMarkAsLearned(userId)
        } else {
            console.error("User to be marked as leaned is not defined");
        }
    }

    // Checks if the selected user is correct
    checkName() {
        const selectedUserId = this.getSelectedId();
        if (selectedUserId === "") {
            return;
        }

        const correct = selectedUserId === this.state.currentUserId;

        fetchCheckResult(this.siteId, this.state.currentUserId, correct);

        if (this.state.soundEnabled) {
            playSound(correct ? SOUNDS.HIT : SOUNDS.MISS);
        }

        this.rollUser = true;
        this.mutateCheckName(correct);
    }


    // RENDER METHODS - Methods that return html for a particular view
    // Should be added to the main render method

    // Renders the GAME view
    renderGame() {
        const { currentUser, progress } = this.state;

        const allUsersCount = this.state.allUsers.length;
        const learnedUsersCount = allUsersCount - this.state.learnUsers.length;

        const imageSrc = this.config.showOfficialPhoto
                ? `/direct/profile/${currentUser.id}/image/official?siteId=${this.siteId}`
                : `/direct/profile/${currentUser.id}/image?siteId=${this.siteId}`;

        return `
            <div class="row">
                <div class="col-sm-4 col-lg-2 col-xl-1 col-xxl-1">
                    <img class="img-responsive img-thumbnail"
                            src="${imageSrc}"
                            alt="${this.tr("user_image_alt")}">
                </div>
                <div class="col-sm-8 col-lg-7 col-xl-6 col-xxl-5">
                    <h2>${this.tr("whats_the_user_name")}</h2>
                    <select id="${this.selectId}" class="form-control">
                        <option value="" disabled selected><option>
                    </select>
                    <div class="act">
                        <button class="active btn-check" disabled data-check>${this.tr("check")}</button>
                        <button class="button" data-reroll>${this.tr("reroll_user")}</button>
                        <button class="button" data-toggle="modal" data-target="#confirm-reset-modal">${this.tr("reset_game")}</button>
                        ${this.renderMuteButton()}
                    </div>
                    ${this.renderFeedbackBanner()}
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12 col-lg-9 col-xl-7 col-xxl-6">
                    <div class="text-center">${this.tr("progress", learnedUsersCount, allUsersCount)}</div>
                    <div class="progress">
                        <div class="progress-bar" role="progressbar" style="width: ${progress}%"
                                aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100">
                        </div>
                    </div>
                </div>
            </div>
            ${this.renderGameResetModal()}
        `;
    }

    // Renders the GAME_OVER view
    renderGameOver() {
        return `
            <div class="sak-banner-success">${this.tr("game_over_info")}</div>
            <div class="act">
                <button class="active" data-toggle="modal" data-target="#confirm-reset-modal">${this.tr("reset_game")}</button>
            </div>
            ${this.renderGameResetModal()}
        `;
    }

    // Renders the NO_STUDENTS view
    renderNoStudentsInfo() {
        return `<div class="sak-banner-info">${this.tr("no_student_info")}</div>`;
    }

    // Renders the GAME_OVER view
    renderGameResetModal() {
        return `
            <div id="confirm-reset-modal" class="modal fade" role="dialog">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">
                                <span class="fa fa-times" aria-hidden="true"></span>
                            </button>
                            <h2 class="modal-title">${this.tr("reset_game_confirm")}</h2>
                        </div>
                        <div class="modal-body">
                            <p class="sak-banner-warn">${this.tr("reset_game_info")}</p>
                        </div>
                        <div class="modal-footer act">
                            <button type="button" class="active" data-reset data-dismiss="modal">${this.tr("reset_game")}</button>
                            <button type="button" class="button" data-dismiss="modal">${this.tr("cancel")}</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    // Renders the feedback banner component
    renderFeedbackBanner() {
        const user =  this.state.previousUser ?? {};

        const strongUserName = `<strong>${user.displayName}</strong>`;

        const { hits, misses } = user;

        const attempts = hits || misses ? hits + misses : null;

        const userNotLearned = !isUserLearned(user, this.config);

        switch(this.state.feedbackState) {
            case FEEDBACK_STATES.NO:
                return "";
            case FEEDBACK_STATES.OK:
                return `
                    <div id="feedback-banner" class="sak-banner-success">
                        <div class="feedback-content">
                            <span>${this.tr("check_hit_info", strongUserName)}</span>
                            <span>${this.tr("user_progress", hits, attempts)}</span>
                        </div>
                        ${userNotLearned ? `<a href="#" role="button" data-mark-learned>${this.tr("mark_learned")}</a>` : ""}
                    </div>
                `;
            case FEEDBACK_STATES.KO:
                return `
                    <div id="feedback-banner" class="sak-banner-error">
                        <div class="feedback-content">
                            <span>${this.tr("check_miss_info", strongUserName)}</span>
                            <span>${this.tr("user_progress", hits, attempts)}</span>
                        </div>
                    </div>
                `;
            default:
                console.error("Unknown feedback this.state", this.state.feedbackState);
                return "";
        }
    }

    // Renders the mute button component
    renderMuteButton() {
        if (this.state.soundEnabled) {
            return `
                <button class="button" title="${this.tr("sound_disable")}" data-mute>
                    <span class="fa fa-volume-up" aria-hidden="true"></span>
                </button>
            `;
        } else {
            return `
                <button class="button" title="${this.tr("sound_enable")}" data-mute>
                    <span class="fa fa-volume-off" aria-hidden="true"></span>
                </button>
            `;
        }
    }

    // CALC METHODS - Methods calculating a property of the state
    // Should be added to updateCalcState in the correct order (dependency respecting)

    calcView() {
        if (this.state.allUsers.length === 0) {
            return VIEWS.NO_STUDENTS;
        } else if (this.state.learnUsers.length === 0) {
            return VIEWS.GAME_OVER;
        } else {
            return VIEWS.GAME;
        }
    }

    calcLearnUsers() {
        return this.state.allUsers.filter((user) => !isUserLearned(user, this.config));
    }

    calcCurrentUser() {
        return this.getUser(this.state.currentUserId);
    }

    calcPreviousUser() {
        return this.getUser(this.state.previousUserId);
    }

    calcUserOptionsData() {
        return this.state.learnUsers.map(({ id, displayName }) => {
            return { id, text: displayName };
        });
    }

    calcProgress() {
        const learnedUserCount = this.state.allUsers.length - this.state.learnUsers.length;

        return 100 * learnedUserCount / this.state.allUsers.length;
    }


    // MUTATE METHODS - Methods that will mutate the state and cause a re-render
    // Mutation should happen by passing a mutator function to the main mutate method

    // Changes the official prop
    mutatePictureType(event) {
        this.mutate((state) => {
            state.official = event.target.id === "official-picture-button";
        });
    }

    // Rolls a new student by setting a new currentUserId
    mutateRerollUser() {
        this.mutate((state) => {
            state.previousUserId = state.currentUserId;
            state.currentUserId = this.getNewStudentId()
        });
    }

    mutateCheckName(correct) {
        this.mutate((state) => {
            state.feedbackState = correct ? FEEDBACK_STATES.OK : FEEDBACK_STATES.KO;
            if (correct) {
                state.currentUser.hits++;
            } else {
                state.currentUser.misses++;
            }
            state.previousUserId = state.currentUserId;
        });
    }

    mutateMute() {
        this.mutate((state) => {
            state.soundEnabled = !state.soundEnabled;
        });
    }

    mutateMarkAsLearned(userId) {
        this.mutate((state) => {
            const user = state.allUsers.find((user) => user.id === userId);

            if (user) {
                user.markedAsLearned = true;
            }
        });
    }

    // EFFECT METHODS - Methods directly altering the dom, without mutating the state

    effectEnableCheckButton() {
        document.querySelector("[data-check]")?.removeAttribute("disabled");
    }

    effectRemoveFeedbackBanner() {
        document.getElementById("feedback-banner")?.remove();
    }

    effectInitSelect() {
        $("#" + this.selectId).select2({
            data: this.state.userOptionsData,
            placeholder: this.tr("user_name_select_placeholder"),
            language: {
                noResults: () => this.tr("no_name_found")
            },
        });
    }

    // GET METHODS - Methods that derive values from the state or the document

    // Gets new random userId
    getNewStudentId() {
        return rollUser(this.state.learnUsers, this.state.previousUserId ?? this.state.currentUserId);
    }

    // Gets selected userId from select
    getSelectedId() {
        return $("#" + this.selectId).select2('data')[0].id ?? "";
    }

    // Gets user with specified userId from state
    getUser(userId) {
        return this.state.allUsers.find((user) => user.id === userId);
    }
}
