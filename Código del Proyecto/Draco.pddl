(define (problem Dragon)
(:domain Historia)

(:objects
Castillo
Montana
Pueblo
Reinaldo
Laura
Draco
Arturo
Hector
)
(:init
(adyacente Castillo Montana)
(adyacente Castillo Pueblo)
(adyacente Pueblo Montana)
(adyacente Pueblo Castillo)
(adyacente Montana Castillo)
(adyacente Montana Pueblo)
(locSegura Castillo)
(enLoc Laura Castillo)
(enLoc Arturo Pueblo)
(enLoc Draco Montana)
(enLoc Hector Pueblo)
(enLoc Reinaldo Castillo)
(esCasa Laura Castillo)
(esCasa Arturo Pueblo)
(esGuarida Draco Montana)
(esCasa Hector Pueblo)
(esCasa Reinaldo Castillo)
(esCaballero Arturo)
(esSecundario Arturo)
(esCaballero Hector)
(esSecundario Hector)
(esRey Reinaldo)
(esPrincipal Reinaldo)
(esPrincesa Laura)
(esPrincipal Laura)
(esDragon Draco)
(esSecundario Draco)
(estaLibre Draco)
(estaLibre Arturo)
(estaLibre Hector)
(vivo Reinaldo)
(vivo Laura)
(vivo Draco)
(vivo Arturo)
(vivo Hector)
)
(:goal 
(and (conPrinc Draco Laura) (enLoc Draco Montana))
)
)
