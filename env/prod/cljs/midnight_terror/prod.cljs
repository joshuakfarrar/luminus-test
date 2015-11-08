(ns midnight-terror.app
  (:require [midnight-terror.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
