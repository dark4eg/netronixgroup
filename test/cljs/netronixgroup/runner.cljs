(ns netronixgroup.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [netronixgroup.core-test]))

(doo-tests 'netronixgroup.core-test)
