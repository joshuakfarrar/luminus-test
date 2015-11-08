(ns midnight-terror.routes.api
  (:require [midnight-terror.layout :as layout]
            [midnight-terror.db.core :as db]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]))

(defn users []
  (ok
    (db/get-users)))

(defroutes api-routes
  (GET "/api/users" [] (users)))
