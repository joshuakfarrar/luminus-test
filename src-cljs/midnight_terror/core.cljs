(ns midnight-terror.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(defn nav-link [uri title page collapsed?]
  [:li {:class (when (= page (session/get :page)) "active")}
   [:a {:href uri
        :on-click #(reset! collapsed? true)}
    title]])

(defn navbar []
  (let [collapsed? (atom true)]
    (fn []
      [:nav.navbar.navbar-inverse.navbar-fixed-top
       [:div.container
        [:div.navbar-header
         [:button.navbar-toggle
          {:class         (when-not @collapsed? "collapsed")
           :data-toggle   "collapse"
           :aria-expanded @collapsed?
           :aria-controls "navbar"
           :on-click      #(swap! collapsed? not)}
          [:span.sr-only "Toggle Navigation"]
          [:span.icon-bar]
          [:span.icon-bar]
          [:span.icon-bar]]
         [:a.navbar-brand {:href "#/"} "midnight-terror"]]
        [:div.navbar-collapse.collapse
         (when-not @collapsed? {:class "in"})
         [:ul.nav.navbar-nav
          [nav-link "#/" "Home" :home collapsed?]
          [nav-link "#/users" "Users" :users collapsed?]]
          [:ul.nav.navbar-nav.navbar-right
           [:li.dropdown
            [nav-link "#/login" "Login" :login collapsed?]]]]]])))

(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn home-page []
  (fetch-docs!)
  (fn []
    [:div.container
     [:div.jumbotron
      [:h1 "Welcome to midnight-terror"]
      [:p "Time to start building your site!"]
      [:p [:a.btn.btn-primary.btn-lg {:href "http://luminusweb.net"} "Learn more »"]]]
     [:div.row
      [:div.col-md-12
       [:h2 "Welcome to ClojureScript"]]]
     (when-let [docs (session/get :docs)]
       [:div.row
        [:div.col-md-12
         [:div {:dangerouslySetInnerHTML
                {:__html (md->html docs)}}]]])]))

(defn user-row [user]
  [:tr { :key (get user "id") }
   [:td (get user "id")]
   [:td (get user "first_name")]
   [:td (get user "last_name")]
   [:td (get user "email")]])

(defn users-table [users]
  [:table.table.table-striped
   [:thead
    [:tr
     [:th "#"]
     [:th "First Name"]
     [:th "Last Name"]
     [:th "E-Mail"]]]
    [:tbody
     (map #(user-row %) users)]])

(defn fetch-users! []
  (GET (str js/context "/api/users") { :handler #(session/put! :users %)}))

(defn users-page []
  (fetch-users!)
  (fn []
    [:div.container
     [users-table (session/get :users)]]))

(defn login-page []
  [:div.container
   [:h1 "Hej world"]
   [:p [:a.btn.btn-primary.btn-lg {:on-click #(js/alert "boink!")} "Click me!"]]])

(def pages
  {:home #'home-page
   :users #'users-page
   :login #'login-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/users" []
  (session/put! :page :users))

(secretary/defroute "/login" []
  (session/put! :page :login))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          EventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (reagent/render [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-components))
