(define (problem Caballero)
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
(enLoc Laura Montana)
(enLoc Arturo Montana)
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
(estaLibre Arturo)
(estaLibre Hector)
(vivo Reinaldo)
(vivo Laura)
(vivo Hector)
(conPrinc Draco Laura)
(secuestrada Laura)
)
(:goal 
(and (esHeroe Hector) (salvada Laura))
)
)
