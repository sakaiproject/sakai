let CACHE_NAME = "appv3";

// caching the static files for offline use
this.addEventListener("install", event => {
    console.log("caching app shell");
    event.waitUntil(
        caches.open(CACHE_NAME).then(
            cache => {
                console.log("HEREEEEEE");
                cache.addAll([
                    '/pwa',
                    '/pwa/static/js/main.6f6f098d.js',
                    '/pwa/static/css/main.f32401f7.css',
                    '/pwa/favicon.ico',
                    '/pwa/logo192.png',
                    '/pwa/manifest.json',
                ])
            }
        )
    )
})

this.addEventListener("fetch", event => {
    // if we are offline, go to the cache directly
    if (!navigator.onLine) {
        if (event.request.method === 'GET') {
            event.respondWith(
                caches
                    .match(event.request).then(resp => {
                        if (resp) {
                            return resp;
                        }
                        let requestUrl = event.request.clone();
                        fetch(requestUrl);
                    })

            );
        }
    }
})

// resend all the requests stored inside indexDB to the backend server
const resendPostRequest = async () => {
    const BASE_NAME = 'backgroundSync';
    const STORE_NAME = 'messages';
    const VERSION = 1;

    const idb = this.indexedDB;
    const request = idb.open(BASE_NAME, VERSION);
    let db = null;
    request.onerror = error => {
        console.warning("An error occured with IndexDB")
        console.warning(error)
    }

    request.onsuccess = () => {
        console.log('Database is opened successfully!', request);
        db = request.result;
        const transaction = db.transaction(STORE_NAME, "readwrite");
        const store = transaction.objectStore(STORE_NAME);
        const query = store.getAll();
        console.log(query);

        query.onsuccess = () => {
            // const BACKEND_HOST = 'http://localhost:8000';
            const BACKEND_HOST = 'https://quick-note--backend.herokuapp.com';
            console.log('query is successful!');
            query.result.forEach(note => {
                const url = BACKEND_HOST + (note.isPrivate ? "/notes" : "/public");
                fetch(url, {
                    method: "POST",
                    body: JSON.stringify(note),
                    headers: {
                        "Content-type": "application/json; charset=UTF-8"
                    }
                }).then(
                    res => res.json()
                ).then(createdNote => {
                    console.log(createdNote);
                });
            })
            store.clear();
        }
    }
}

// when the user resume the internet connection, resend all the requests in indexDB
this.addEventListener('sync', async (event) => {
    if (event.tag === 'back-sync') {
        console.log('[Service Worker] is background syncing...');
        await resendPostRequest();
        this.location.reload();
    }
})

