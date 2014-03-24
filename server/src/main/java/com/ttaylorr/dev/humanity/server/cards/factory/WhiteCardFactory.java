package com.ttaylorr.dev.humanity.server.cards.factory;

import com.oracle.javafx.jmx.json.JSONDocument;
import com.ttaylorr.dev.humanity.server.cards.HumanityCard;
import com.ttaylorr.dev.humanity.server.cards.WhiteCard;
import com.ttaylorr.dev.humanity.server.cards.WhiteCardDeck;

import java.io.File;

public class WhiteCardFactory extends CardFactory<WhiteCard> {

    public WhiteCardFactory(File f) {
        super(f);
    }

    @Override
    public void parse() {
        for(JSONDocument doc : this.getConvertedDocuments()) {
            if (doc.getString(CardFactory.CARD_TYPE).equals("A")) {
                String message = doc.getString(CardFactory.TEXT);
                HumanityCard.Expansion expansion = HumanityCard.Expansion.forString(doc.getString(CardFactory.EXPANSION));

                WhiteCard card = new WhiteCard(message, expansion);
                this.getCards().add(card);
            }
        }
    }

    @Override
    public WhiteCardDeck build() {
        return new WhiteCardDeck(this.getCards());
    }

}
