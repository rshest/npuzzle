(ns app
    (:require-macros [cljs.core.async.macros
                      :refer [go go-loop]])
    (:require [cljs.core.async :as async
               :refer [>! <! timeout]]
              [clojure.string :as str]
              [goog.dom :as dom]
              [goog.dom.classes :as classes]
              [goog.events :as events]
              [goog.style :as style]
              [goog.array]
              [npuzzle]))

(def block-width 50)
(def block-margin 2)
(def fps 40)

(def ^:dynamic *board*)
(def ^:dynamic *nmoves* 0)
(def ^:dynamic *solving* false)

(defn put-block [block x y]
  (style/setStyle
   block (clj->js
          {:left (str (* x block-width) "px")
           :top  (str (* y block-width) "px")})))

(defn transition
  [duration f]
  (let [n      (* duration fps)
        dt     (/ 1000 fps)]
    (go-loop [i 0]
      (f (/ i n))
      (<! (timeout dt))
      (if (< i n) (recur (inc i))))))

(defn lerp
  "Linear interpolation, y = (1 - t)a + tb"
  [a b t]
  (+ (* t b) (* (- 1 t) a)))

(defn get-block-elem [num]
  (->> "board"
       dom/getElement
       dom/getChildren
       goog.array/toArray
       (dom/getElement (str num))))

(defn play-move [num]
  (let [block    (get-block-elem num)
        [x0 y0]  (npuzzle/get-num-pos *board* num)
        [x1 y1]  (*board* :blank-pos)
        dmoves   (dom/getElement "moves")]
  (if (npuzzle/neighbors? [x0 y0] [x1 y1])
    (do
      (set! *board* (npuzzle/move *board* [x0 y0]))
      (set! *nmoves* (inc *nmoves*))
      (dom/setTextContent
       dmoves (str (dom/getTextContent dmoves) " " num))
      (dom/setTextContent (dom/getElement "nmoves") (str *nmoves*))
      (transition
       0.1 #(put-block block (lerp x0 x1 %) (lerp y0 y1 %)))))))

(defn play-moves [moves]
  (go-loop [i 0]
          (when (< i (count moves))
                (<! (play-move (nth moves i)))
                (if *solving* (recur (inc i))))))

(defn solve-board
  []
  (do
    (set! *solving* (not *solving*))
    ((if *solving* classes/add classes/remove)
     (dom/getElement "solve") "btn_sel")
    (if *solving* (play-moves (npuzzle/solve-nums *board*)))))

(defn onclick
  [elem callback]
  (.listen goog.events elem
           goog.events.EventType.CLICK callback))

(defn make-block [idx num]
  (let [nstr  (str num)
        cl    (if (> num 0) "block" "hidden")
        block (dom/createDom
              "div" (clj->js {:class cl :id nstr}) nstr)
        [x y] (npuzzle/idx->pos *board* idx)]
    (put-block block x y)
    (onclick block #(if (not *solving*) (play-move num)))
    block))

(defn make-board-control [board]
  (let [n (*board* :n)
        s (str (+ (* n block-width) block-margin) "px")
        board  (dom/getElement "board")
        blocks (map-indexed make-block (*board* :cells))]
    (dom/removeChildren board)
    (style/setStyle board (clj->js {:width s :height s}))
    (doseq [b blocks] (dom/append board b))))

(defn get-url-params
  "Returns a map of URL-encoded parameters"
  []
  (let [url      js/window.location.href
        [_ par]  (str/split url "?")
        params   (str/split par "&")]
    (into {} (map #(str/split % "=") params))))

(def preset-boards {
  3 [1 6 0
     8 4 2
     7 5 3]
  4 [6  14 3  13
     7  1  0  5
     8  10 2  12
     15 9  11 4 ]
  6 [1   10  25  33  12  19
     14  2   34  26  17  6
     18  9   31  4   30  16
     28  3   21  22  35  24
     13  32  7    0  5   23
     27  8   20  11  29  15] })

(defn parse-nums
  [nums]
  (map js/parseInt (str/split nums ",")))

(defn init-board
  [params]
  (let [n     (js/parseInt (get params "n" "6"))
        nums  (params "nums")
        cells (do (js/console.log (str n " " nums))
                (if nums (parse-nums nums) (preset-boards n)))]
    (set! *board* (npuzzle/make-board cells))))

(set! (.-onload js/window)
      #(do
         (init-board (get-url-params))
         (onclick (dom/getElement "solve") solve-board)
         (make-board-control *board*)))

