package com.lomork.house_of_cards.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lomork.house_of_cards.data.AppStore
import com.lomork.house_of_cards.data.PlayerProfile
import com.lomork.house_of_cards.data.SeedData
import com.lomork.house_of_cards.data.StoreItemDef
import com.lomork.house_of_cards.nav.Navigator
import com.lomork.house_of_cards.theme.Hoc
import com.lomork.house_of_cards.ui.HocButton
import com.lomork.house_of_cards.ui.HocCard
import com.lomork.house_of_cards.ui.PokerChip
import com.lomork.house_of_cards.ui.SectionHeader
import com.lomork.house_of_cards.ui.StatTile

private fun cashLabel(cents: Int): String = "$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

@Composable
fun StoreScreen(store: AppStore, nav: Navigator) {
    val p = store.profile
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Store", style = MaterialTheme.typography.headlineLarge, color = Hoc.Gold)
        Row(Modifier.fillMaxWidth()) {
            StatTile("Coins", p.coins.toString(), Modifier.fillMaxWidth())
        }

        SectionHeader("Chips")
        val chips = SeedData.storeItems.filter { it.type == "chip" && it.id != "chip_default" }
        chips.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { item -> ChipItem(item, p, store, Modifier.weight(1f)) }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        SectionHeader("Real money")
        HocCard {
            Text(
                "Real-money items are coming soon. For now, everything is purchasable with coins you earn by playing.",
                style = MaterialTheme.typography.bodySmall,
                color = Hoc.TextMuted,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChipItem(item: StoreItemDef, p: PlayerProfile, store: AppStore, modifier: Modifier) {
    HocCard(modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PokerChip(item, size = 56.dp)
            Text(item.name, style = MaterialTheme.typography.titleMedium, color = Hoc.TextPrimary)
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = Hoc.TextMuted, textAlign = TextAlign.Center)
            Text("${item.coinPrice} coins", style = MaterialTheme.typography.labelLarge, color = Hoc.Gold)
            if (item.cashPriceUsd != null) {
                Text("or ${cashLabel(item.cashPriceUsd)} · soon", style = MaterialTheme.typography.labelSmall, color = Hoc.TextMuted)
            }
            when {
                item.id == p.equippedChipId -> HocButton("Equipped", enabled = false) {}
                item.id in p.ownedItemIds -> HocButton("Equip", secondary = true) { store.equipChip(item.id) }
                else -> HocButton("Buy") {
                    if (store.spendCoins(item.coinPrice)) {
                        store.unlockItem(item.id)
                        store.equipChip(item.id)
                    }
                }
            }
        }
    }
}