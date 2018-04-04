(function ($) {

    portal.connectionManager = portal.connectionManager || {};

    var connectionTemplate = Handlebars.templates['connection-manager-connection'];
    var searchResultTemplate = Handlebars.templates['connection-manager-searchresult'];

    var indexedCurrentConnections = {};
    var indexedPendingConnections = {};

    var countPending = function () { return Object.keys(indexedPendingConnections).length; };
    var indexedSearchResults = {};
    var lastSearchResults = {};

    $PBJQ(document).ready(function () {

        portal.i18n.loadProperties({
            resourceClass: 'org.sakaiproject.portal.api.PortalService',
            resourceBundle: 'connection-manager',
            namespace: 'connection-manager',
            callback: function () {

                portal.i18n.loadProperties({
                    resourceClass: 'org.sakaiproject.portal.api.PortalService',
                    resourceBundle: 'profile-popup',
                    namespace: 'connection-manager'
                });
            }
        });
    });

    var currentTotal = 0;
    var currentConnections = [];

    var CONNECTIONS = 'connections';
    var SEARCH_RESULTS = 'searchresults';

    var currentState = CONNECTIONS;

    var shown = 0;
    var pendingTabBaseHtml = '';

    portal.connectionManager.show = function (options) {

        var connectionManager = $PBJQ('#connection-manager');

        connectionManager.modal({
            width: 320
        });

        var connectionsView = $PBJQ('#connection-manager-connectionsview');
        var searchResultsView = $PBJQ('#connection-manager-searchresultsview');
        var searchResultsCount = $PBJQ('#connection-manager-searchresultsview-count');

        var currentTab = $PBJQ('#connection-manager-navbar-current > span > a');
        var pendingTab = $PBJQ('#connection-manager-navbar-pending > span > a');

        var updateSearchResultsCount = function (count) {

                if (count === 0) {
                    searchResultsCount.html(portal.i18n.tr('connection-manager', 'connection_manager_no_results'));
                } else {
                    var translateOptions
                        = {count: count, criteria: portal.connectionManager.searchCriteria};
                    var countMessage = (count > 1)
                        ? portal.i18n.tr('connection-manager', 'connection_manager_results_count', translateOptions)
                        : portal.i18n.tr('connection-manager', 'connection_manager_result_count', translateOptions);

                    searchResultsCount.html(countMessage);
                }
            };

        var showCurrentTab = function () {

                pendingConnectionsWrapper.hide();
                currentConnectionsWrapper.show();
                connectionsView.show();
                searchResultsView.hide();
                currentTab.parent().addClass('current');
                pendingTab.parent().removeClass('current');
            };

        var showPendingTab = function () {

                currentConnectionsWrapper.hide();
                pendingConnectionsWrapper.show();
                connectionsView.show();
                searchResultsView.hide();
                pendingTab.parent().addClass('current');
                currentTab.parent().removeClass('current');
            };

        var addPendingAndShowTab = function (friendId, displayName) {

                var countBefore = countPending();

                $PBJQ('#connection-manager-connection-' + friendId).remove();
                $PBJQ('#connection-manager-connectionsview-searchresult-' + friendId).remove();

                if (searchResults.children().length === 0) {
                    searchResultsWrapper.hide();
                }

                var connection = indexedSearchResults[friendId];
                connection.connected = false;
                connection.hideConnect = true;
                connection.outgoing = true;

                indexedPendingConnections[friendId] = connection;

                var markup = connectionTemplate(connection);
                if (countBefore === 0) {
                    pendingConnectionsDiv.show().html('');
                    noPendingConnectionsDiv.hide();
                }
                pendingConnectionsDiv.append(markup);
                updatePendingTabText();

                if (currentState === CONNECTIONS
                        || (currentState == SEARCH_RESULTS && moreSearchResults.children().length === 0)) {
                    showPendingTab();
                }

                $PBJQ('#connection-manager-cancel-button-' + friendId).click(cancelHandler);
            };

        currentTab.click(function (e) { showCurrentTab(); });
        pendingTab.click(function (e) { showPendingTab(); });

        var searchResultsWrapper = $PBJQ('#connection-manager-connectionsview-searchresults-wrapper');
        var searchResults = $PBJQ('#connection-manager-connectionsview-searchresults');
        var moreSearchResults = $PBJQ('#connection-manager-searchresultsview-results');

        connectionManager.click(function (e) {

            if (e.target.id !== 'connection-manager-connectionsview-searchbox') {
                var wrapperRect = searchResultsWrapper[0].getBoundingClientRect();
                var searchBoxRect = searchBox[0].getBoundingClientRect();
                if (e.pageX < wrapperRect.left || e.pageY < wrapperRect.top
                        || e.pageX > (wrapperRect.left + wrapperRect.width)
                        || e.pageY > (wrapperRect.top + wrapperRect.height)) {
                    searchResultsWrapper.hide();
                }
            }
        });

        var currentConnectionsDiv = $PBJQ('#connection-manager-current-connections');
        var currentConnectionsWrapper = $PBJQ('#connection-manager-current-connections-wrapper');
        var noCurrentConnectionsDiv = $PBJQ('#connection-manager-no-current-connections-wrapper');
        var pendingConnectionsDiv = $PBJQ('#connection-manager-pending-connections');
        var pendingConnectionsWrapper = $PBJQ('#connection-manager-pending-connections-wrapper');
        var noPendingConnectionsDiv = $PBJQ('#connection-manager-no-pending-connections-wrapper');
        var searchBox = $PBJQ('#connection-manager-connectionsview-searchbox');
        searchBox.clearSearch({callback: function () { searchResultsWrapper.hide(); }});
        var moreSearchBox = $PBJQ('#connection-manager-searchresultsview-searchbox');

        if (shown == 0) {
            pendingTabBaseHtml = pendingTab.html();
            shown += 1;
        }

        var moveFromPendingToCurrent = function (friendId) {

                $PBJQ('#connection-manager-connection-' + friendId).remove();
                if (currentTotal == 0) {
                    currentConnectionsDiv.html('');
                }
                var connection = indexedPendingConnections[friendId];
                connection.outgoing = false;
                connection.incoming = false;
                connection.connected = true;
                var markup = connectionTemplate(connection);
                currentConnectionsDiv.append(markup);
                currentTotal += 1;
                $PBJQ('#connection-manager-remove-button-' + friendId).click(removeHandler);
                noCurrentConnectionsDiv.hide();
                delete indexedPendingConnections[friendId];
                indexedCurrentConnections[friendId] = connection;
                updatePendingTabText();
                showCurrentTab();
            };

        var acceptHandler = function () {

                var friendId = this.dataset.userId;
                var displayName = this.dataset.displayName;
                $PBJQ.ajax('/direct/profile/' + portal.user.id + '/confirmFriendRequest?friendId=' + friendId
                        , {cache: false})
                    .done(function (data) {
                        moveFromPendingToCurrent(friendId);
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        console.log('ERROR: failed to confirm request from \'' + displayName + '\'. errorThrown: ' + errorThrown);
                    });
            };

        var removePending = function (friendId) {

                $PBJQ('.connection-manager-connection-' + friendId).remove();
                delete indexedPendingConnections[friendId];
                updatePendingTabText();
            };

        var ignoreHandler = function () {

                var friendId = this.dataset.userId;
                var displayName = this.dataset.displayName;
                $PBJQ.ajax('/direct/profile/' + portal.user.id + '/ignoreFriendRequest?friendId=' + friendId, {cache: false})
                    .done(function (data) {
                        removePending(friendId);
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        console.log('ERROR: failed to ignore request from \'' + displayName + '\'. errorThrown: ' + errorThrown);
                    });
            };

        var connectHandler = function () {

                var friendId = this.dataset.userId;
                var displayName = this.dataset.displayName;
                $PBJQ.ajax('/direct/profile/' + portal.user.id + '/requestFriend?friendId=' + friendId
                        , {cache: false})
                    .done(function (data) {

                        addPendingAndShowTab(friendId, displayName);
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        console.log('ERROR: failed to request connection to \'' + displayName + '\'. errorThrown: ' + errorThrown);
                    });
            };

        var removeCurrent = function (friendId) {

                $PBJQ('#connection-manager-connection-' + friendId).remove();
                currentTotal -= 1;
                if (currentTotal == 0) {
                    noCurrentConnectionsDiv.show();
                }
                delete indexedCurrentConnections[friendId];
            };

        var removeHandler = function () {

                var friendId = this.dataset.userId;
                var displayName = this.dataset.displayName;
                if (confirm(portal.i18n.tr('connection-manager', 'connection_manager_remove_confirm', {displayName: displayName}))) {
                    $PBJQ.ajax('/direct/profile/' + portal.user.id + '/removeFriend?friendId=' + friendId, {cache: false})
                        .done(function (data) {
                            removeCurrent(friendId);
                        })
                        .fail(function (jqXHR, textStatus, errorThrown) {
                            console.log('ERROR: failed to remove \'' + displayName + '\'. errorThrown: ' + errorThrown);
                        });
                }
        };

        var cancelHandler = function () {

                var friendId = this.dataset.userId;
                var displayName = this.dataset.displayName;

                $PBJQ.ajax('/direct/profile/' + friendId + '/ignoreFriendRequest?friendId=' + portal.user.id, {cache: false})
                    .done(function (data) {

                        $PBJQ('.connection-manager-connection-' + friendId).remove();
                        delete indexedPendingConnections[friendId];
                        updatePendingTabText();
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        console.log('ERROR: failed to ignore request from \'' + displayName + '\'. errorThrown: ' + errorThrown);
                    });
            };

        var updatePendingTabText = function () {

                var pendingTotal = countPending();
                if (pendingTotal === 0) {
                    pendingTab.html(pendingTabBaseHtml);
                    pendingConnectionsDiv.html('').hide();
                    noPendingConnectionsDiv.show();
                } else {
                    pendingTab.html(pendingTabBaseHtml + ' (' + pendingTotal + ')');
                }
            };

        var search = function (criteria, showFullConnections) {

                var container = (showFullConnections) ? moreSearchResults : searchResults;

                if (criteria.length < 4) {
                    container.html('');
                    if (!showFullConnections) {
                        searchResultsWrapper.hide();
                    } else {
                        updateSearchResultsCount(0);
                    }
                    return;
                }

                portal.connectionManager.searchCriteria = criteria;

                var template = (showFullConnections) ? connectionTemplate : searchResultTemplate;

                $PBJQ.ajax('/direct/portal/connectionsearch.json?query=' + criteria, {cache: false})
                    .done(function (results) {

                        container.html('');

                        if (results.length === 0) {
                            if (!showFullConnections) {
                                searchResultsWrapper.hide();
                            }
                            return;
                        }

                        if (!showFullConnections) {
                            searchResultsWrapper.show();
                        }

                        var markup = '';
                        lastSearchResults = results;

                        indexedSearchResults = {};
                        lastSearchResults.forEach(function (r) {
                            indexedSearchResults[r.uuid] = r;
                        });

                        if (showFullConnections) {
                            lastSearchResults.forEach(function (result, i) {
                                    markup += template(result);
                                });
                        } else {
                            lastSearchResults.slice(0, 5).forEach(function (result, i) {
                                    markup += template(result);
                                });
                        }

                        if (showFullConnections) {
                            updateSearchResultsCount(lastSearchResults.length);
                        }

                        container.html(markup);

                        if (container.children().length > 0) {
                            container.show();
                        }

                        $PBJQ(document).ready(function () {

                            if (!showFullConnections) {
                                profile.attachPopups($PBJQ('.profile-popup'), {connect: addPendingAndShowTab, cancel: removePending, accept: moveFromPendingToCurrent, ignore: removePending, remove: removeCurrent});
                            } else {
                                $PBJQ('.connection-manager-connect-button').click(connectHandler);
                            }

                            $PBJQ('#connection-manager-connectionsview-searchresults-more').click(function (e) {

                                currentState = SEARCH_RESULTS;

                                searchResults.html('');
                                searchResultsWrapper.hide();
                                connectionsView.hide();
                                searchResultsView.show();
                                moreSearchBox.val(portal.connectionManager.searchCriteria);
                                searchBox.val('');
                                $PBJQ(document).ready(function () {

                                    updateSearchResultsCount(lastSearchResults.length);

                                    var markup = '';
                                    lastSearchResults.forEach(function (result, i) {
                                        result.facebookSet = result.socialNetworkingInfo.facebookUrl;
                                        result.twitterSet = result.socialNetworkingInfo.twitterUrl;
                                        result.moreResult = true;
                                        if (indexedCurrentConnections.hasOwnProperty(result.uuid)) {
                                            result.connected = true;
                                            result.hideConnect = true;
                                        }
                                        if (indexedPendingConnections.hasOwnProperty(result.uuid)) {
                                            result.connected = false;
                                            result.hideConnect = true;
                                            result.outgoing = indexedPendingConnections[result.uuid].outgoing;
                                            result.incoming = indexedPendingConnections[result.uuid].incoming;
                                        }
                                        markup += connectionTemplate(result);
                                    });
                                    moreSearchResults.html(markup);
                                    $PBJQ(document).ready(function () {

                                        $PBJQ('.connection-manager-accept-button').click(acceptHandler);
                                        $PBJQ('.connection-manager-ignore-button').click(ignoreHandler);
                                        $PBJQ('.connection-manager-connect-button').click(connectHandler);
                                        $PBJQ('.connection-manager-remove-button').click(removeHandler);
                                        $PBJQ('.connection-manager-cancel-button').click(cancelHandler);
                                    });
                                });
                            });

                            $PBJQ('#connection-manager-backtoconnections-link').click(function (e) {

                                currentState = CONNECTIONS;
                                searchResultsView.hide();
                                connectionsView.show();
                                searchResultsWrapper.hide();
                                searchResults.html('');
                                searchBox.val('');
                            });
                        }); // document.ready
                    }); // ajax call
            }; // search

        // Load up the current connections
        $PBJQ.ajax('/direct/profile/' + portal.user.id + '/connections.json', {cache: false})
            .done(function (data) {

                indexedCurrentConnections = {};

                currentTotal = data.length;

                // Reset the search filter
                searchUserIdFilter = [portal.user.id];

                currentConnectionsDiv.html('');

                currentConnections = data;
                if (currentConnections.length == 0) {
                    noCurrentConnectionsDiv.show();
                } else {
                    noCurrentConnectionsDiv.hide();
                }

                data.forEach(function (connection) {

                    connection.facebookSet = connection.socialNetworkingInfo.facebookUrl;
                    connection.twitterSet = connection.socialNetworkingInfo.twitterUrl;
                    connection.current = true;
                    connection.connected = true;
                    connection.incoming = false;
                    connection.hideConnect = true;
                    indexedCurrentConnections[connection.uuid] = connection;
                    currentConnectionsDiv.append(connectionTemplate(connection));
                });

                $PBJQ(document).ready(function () {
                    $PBJQ('.connection-manager-remove-button').click(removeHandler);
                });
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.log('ERROR: failed to get current connections. errorThrown: ' + errorThrown);
            });

        var pendingConnectionsCallback = function (connections) {

                if (connections.length == 0) {
                    noPendingConnectionsDiv.show();
                    pendingConnectionsDiv.hide();
                } else {
                    noPendingConnectionsDiv.hide();
                    pendingConnectionsDiv.show().html('');
                }

                connections.forEach(function (connection) {

                    connection.hideConnect = true;
                    connection.pending = true;
                    pendingConnectionsDiv.append(connectionTemplate(connection));
                    indexedPendingConnections[connection.uuid] = connection;
                });

                if (connections.length > 0) {
                    // Update the pending tab
                    pendingTab.html(pendingTabBaseHtml + ' (' + connections.length + ')');
                } else {
                    pendingTab.html(pendingTabBaseHtml);
                }

                $PBJQ(document).ready(function () {

                    $PBJQ('.connection-manager-accept-button').click(acceptHandler);
                    $PBJQ('.connection-manager-ignore-button').click(ignoreHandler);
                    $PBJQ('.connection-manager-cancel-button').click(cancelHandler);
                }); // document.ready
            }; // pendingConnectionsCallback

        // Load up the pending connections
        $PBJQ.ajax('/direct/profile/' + portal.user.id + '/incomingConnectionRequests.json', {cache: false})
            .done(function (data) {

                indexedPendingConnections = {};

                data.forEach(function (connection) {
                    connection.incoming = true;
                });

                $PBJQ.ajax('/direct/profile/' + portal.user.id + '/outgoingConnectionRequests.json', {cache: false})
                    .done(function (outgoing) {

                        outgoing.forEach(function (connection) {

                            connection.outgoing = true;
                            data.push(connection);
                        });

                        pendingConnectionsCallback(data);
                    })
                    .fail(function (jqXHR, textStatus, errorThrown) {
                        console.log('Failed to get outgoing requests. errorThrown: ' + errorThrown);
                    });
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                console.log('Failed to get incoming requests. errorThrown: ' + errorThrown);
            });

        searchBox.keyup(function (e) { search(this.value, false); });
        searchBox.keydown(function (e) {

            if (e.which == 13 && this.value.length >= 4) {
                $PBJQ('#connection-manager-connectionsview-searchresults-more').click();
            }
        });
        moreSearchBox.keyup(function (e) { search(this.value, true); });

        if (options && options.state === 'pending') {
            showPendingTab();
        }
    }; // portal.connectionManager.show

    $PBJQ('#Mrphs-userNav__submenuitem--connections').click(portal.connectionManager.show);
}) ($PBJQ);
