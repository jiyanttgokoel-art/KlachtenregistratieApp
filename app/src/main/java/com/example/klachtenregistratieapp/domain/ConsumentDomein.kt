package com.example.klachtenregistratieapp.domain

class ConsumentDomein(
    partijID: Int,
    partijNaam: String,
    partijContact: String,
    private val consumentNaam: String
) : Partij(partijID, partijNaam, partijContact) {

    fun getConsumentNaam(): String {
        return consumentNaam
    }

    override fun toonGegevens(): String {
        return "Consument: $consumentNaam | ${super.toonGegevens()}"
    }

    override fun rolBeschrijving(): String {
        return "Deze partij heeft de rol consument."
    }
}
//Volgens de herkansing moest ik ook voldoen aan blok 2 eisen zoals overerving en OOP-principes.

//Generalisatie / specialisatie
//Dit is verwerkt in de domain-laag met Partij als superklasse en ConsumentDomein en VerkoperDomein als subklassen.

//Protected / encapsulatie
//Ik heb hier private attributen met getters gebruikt in plaats van protected, om data beter af te schermen.

//Overriding
//Dit gebeurt in de subklassen waar methodes zoals toonGegevens() worden overschreven.

//Polymorfie
//Door te werken met de superklasse Partij kunnen verschillende types (consument/verkoper) hetzelfde aangeroepen worden maar anders gedrag tonen.

//Abstractie
//Partij is een abstracte klasse met een abstracte methode die door de subklassen wordt ingevuld.

//GUI
//De app heeft meerdere schermen en gebruikt verschillende componenten zoals ListView, Spinner en formulieren.

//Scheiding model/view
//Dit is gedaan via aparte packages: model, dao, database en activity.

//Exceptions
//Fouten worden opgevangen met try/catch en de gebruiker krijgt duidelijke meldingen.