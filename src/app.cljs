(ns app
    (:require-macros [cljs.core.async.macros
                      :refer [go go-loop]])
    (:require [cljs.core.async :as async
               :refer [>! <! timeout]]
              [goog.dom :as dom]
              [goog.events :as events]
              [goog.style :as style]
              [npuzzle]))

(def BLOCK-WIDTH 50)
(def BLOCK-MARGIN 2)
(def BOARD)
(def BLOCKS)
(def NMOVES 0)
(def FPS 40)

(def b3x3 [1 6 0
           8 4 2
           7 5 3])

(def b4x4 [6  14 3  13
           7  1  0  5
           8  10 2  12
           15 9  11 4 ])

(def b6x6 [1   10  25  33  12  19
           14  2   34  26  17  6
           18  9   31  4   30  16
           28  3   21  22  35  24
           13  32  7    0  5   23
           27  8   20  11  29  15])

(defn move-block [block x y]
  (style/setStyle block (clj->js
                         {:left (str (* x BLOCK-WIDTH) "px")
                          :top  (str (* y BLOCK-WIDTH) "px")})))

(defn transition [duration f]
  (let [n      (* duration FPS)
        dt     (/ 1000 FPS)]
    (go-loop [i 0]
           (f (/ i n))
           (<! (timeout dt))
           (if (< i n) (recur (+ i 1))))))

(defn lerp [a b t] (+ (* t b) (* (- 1 t) a)))

(defn play-moves [[num xs] n]
  (let [block    (BLOCKS (- num 1))
        from     (npuzzle/get-idx BOARD num)
        to       (npuzzle/get-idx BOARD 0)
        [x0 y0]  (npuzzle/get-pos n from)
        [x1 y1]  (npuzzle/get-pos n to)]
  (if (npuzzle/can-move n x0 y0 x1 y1) (do
    (set! BOARD (npuzzle/move BOARD from to))
    (set! NMOVES (inc NMOVES))
    (transition
     0.1 #(move-block block (lerp x0 x1 %) (lerp y0 y1 %)))
    (play-moves xs)))))


(defn click-block [n num] (play-moves [num] n))


(defn make-block [n idx num]
  (let [block (dom/createDom
              "div" (clj->js {:class "block"}) (str num))
        [x y] (npuzzle/get-pos n idx)]
    (do (move-block block x y))
    (.listen goog.events block
             goog.events.EventType.CLICK
             #(click-block n num))
    (if (> num 0) block)))


(defn make-board [n cells]
  (let [s (str (+ (* n BLOCK-WIDTH) BLOCK-MARGIN) "px")
        board  (dom/getElement "board")
        blocks (map-indexed (partial make-block n) cells)]
    (do (dom/removeChildren board))
    (style/setStyle board (clj->js {:width s :height s}))
    (doseq [b blocks] (dom/append board b))
    (to-array blocks)))

(set! BOARD b4x4)
(set! (.-onload js/window) #(set! BLOCKS (make-board 4 BOARD)))
