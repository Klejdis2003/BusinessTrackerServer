package com.klejdis.services.services

import com.klejdis.services.dto.*
import com.klejdis.services.filters.ExpenseFilterCategory
import com.klejdis.services.filters.Filter
import com.klejdis.services.filters.OrderFilterCategory
import com.klejdis.services.model.Expense
import com.klejdis.services.model.Order
import com.klejdis.services.model.OrderItem
import com.klejdis.services.repositories.ExpenseRepository
import com.klejdis.services.repositories.OrderRepository
import com.klejdis.services.util.DatePeriod

/**
 * Service class for analyzing data from order and expense repositories and creating some insights.
 * @param orderRepository The repository for orders.
 * @param expenseRepository The repository for expenses.
 * @param loggedInEmail The email of the logged in user.
 */
class AnalyticsService(
    private val orderRepository: OrderRepository,
    private val expenseRepository: ExpenseRepository,
    private val itemMapper: ItemMapper,
    private val loggedInEmail: String
) {

    /**
     * @param datePeriod The time period to filter orders by.
     * @return A list of orders made within the specified time period.
     */
    private suspend fun getOrders(datePeriod: DatePeriod): List<Order>{
        val filters = buildList {
            if(datePeriod.hasStartBound()) {
                add(Filter(
                    OrderFilterCategory.MinDate.typeName,
                    datePeriod.startDate.toString(),
                ))
            }
            if(datePeriod.hasEndBound()) {
                add(Filter(
                    OrderFilterCategory.MaxDate.typeName,
                    datePeriod.endDate.toString(),
                ))
            }
        }
        return orderRepository.filterByBusinessOwnerEmail(
            loggedInEmail,
            filters
        )
    }

    /**
     * @param datePeriod The time period to filter expenses by.
     * @return A list of expenses made within the specified time period.
     */
    private suspend fun getExpenses(datePeriod: DatePeriod): List<Expense> {
        val filters = buildList {
            if(datePeriod.hasStartBound()) {
                add(Filter(
                    ExpenseFilterCategory.MinDate.typeName,
                    datePeriod.startDate.toString(),
                ))
            }
            if(datePeriod.hasEndBound()) {
                add(Filter(
                    ExpenseFilterCategory.MaxDate.typeName,
                    datePeriod.endDate.toString(),
                ))
            }
        }
        return expenseRepository.filterByBusinessOwnerEmail(
            loggedInEmail,
            filters
        )
    }

    /**
     * @param orderItem The order item to calculate the profit for.
     * @return The profit generated from the specified order item.
     */
    private fun getOrderItemProfit(orderItem: OrderItem) =
        (orderItem.item.price - orderItem.item.purchasePrice) * orderItem.quantity

    /**
     * Adjust the limit to ensure it is within the allowed range and does not exceed bounds
     * @param orders The list of orders to calculate the top customers from.
     * @param limit The maximum number requested
     * @return The adjusted limit
     */
    private fun adjustLimitToOrdersCount(orders: List<Order>, limit: Int): Int {
        val maxLimit = 50
        val adjustedLimit = if (limit > orders.size) orders.size else limit
        return if (adjustedLimit > maxLimit) maxLimit else adjustedLimit
    }

    /**
     * @param orders The list of orders to calculate the top customers from.
     * @param limit The maximum number of top customers to return.
     * @return A list of top customers entries based on the specified list of orders. Each entry contains a customer and their total profit.
     */
    private fun getMostProfitableCustomers(orders: List<Order>, limit: Int = 10): List<MostProfitableCustomerDto> {
        val adjustedLimit = adjustLimitToOrdersCount(orders, limit)
        val customerProfits = orders.groupBy { it.customer }
            .mapValues { (_, customerOrders) ->
                customerOrders.sumOf { order ->
                    order.items.sumOf { getOrderItemProfit(it) }
                }
            }
        return customerProfits.entries
            .sortedByDescending { it.value }
            .take(adjustedLimit)
            .map {
                MostProfitableCustomerDto(
                    CustomerDto.fromEntity(it.key),
                    it.value
                )
            }
    }



    /**
     * @param orders The list of orders to calculate the most popular item from.
     * @param limit The maximum number of most popular items to return.
     * @return A list of most popular item entries based on the specified list of orders.
     * Each entry contains an item and the number of times it was ordered.
     */
    private fun getMostPopularItems(orders: List<Order>, limit: Int = 10): List<MostPopularItemDto> {
        val adjustedLimit = adjustLimitToOrdersCount(orders, limit)
        val itemQuantities = orders.flatMap { it.items }
            .groupBy { it.item }
            .mapValues { (_, orderItems) -> orderItems.sumOf { it.quantity } }

        return itemQuantities.entries
            .sortedByDescending { it.value }
            .take(adjustedLimit)
            .map {
                MostPopularItemDto(
                    itemMapper.toItemDto(it.key),
                    it.value
                )
            }
    }

    /**
     * @param orders The list of orders to calculate the most profitable item from.
     * @param limit The maximum number of most profitable items to return.
     * @return A list of most profitable item entries based on the specified list of orders.
     * Each entry contains an item and the profit generated from it.
     */
    private fun getMostProfitableItems(orders: List<Order>, limit: Int = 10): List<MostProfitableItemDto> {
        val adjustedLimit = adjustLimitToOrdersCount(orders, limit)
        val itemProfits = orders.flatMap { it.items }
            .groupBy { it.item }
            .mapValues { (_, orderItems) ->
                orderItems.sumOf {
                    (it.item.price - it.item.purchasePrice) * it.quantity
                }
            }

        return itemProfits.entries
            .sortedByDescending { it.value }
            .take(adjustedLimit)
            .map {
                MostProfitableItemDto(
                    itemMapper.toItemDto(it.key),
                    it.value
                )
            }
    }

    /**
     * @param orders The list of orders to calculate the most popular item from.
     * @return The most popular item from the specified list of orders or null if the list is empty.
     */
    private fun getMostPopularItem(orders: List<Order>): MostPopularItemDto? =
        getMostPopularItems(orders, 1).firstOrNull()

    /**
     * @param orders The list of orders to calculate the most profitable customer from.
     * @return The most profitable customer from the specified list of orders or null if the list is empty.
     */
    private fun getMostProfitableCustomer(orders: List<Order>): MostProfitableCustomerDto? =
        getMostProfitableCustomers(orders, 1).firstOrNull()

    /**
     * @param orders The list of orders to calculate the most profitable item from.
     * @return The most profitable item from the specified list of orders or null if the list is empty.
     */
    private fun getMostProfitableItem(orders: List<Order>): MostProfitableItemDto? =
        getMostProfitableItems(orders, 1).firstOrNull()


        /**
     * @param orders The list of orders to calculate the total profits from.
     * @return The total profits generated from the specified list of orders.
     */

    private fun calculateOrderProfits(orders: List<Order>) =
        orders.sumOf {
            it.items.sumOf { orderItem ->
                val itemProfit = (orderItem.item.price - orderItem.item.purchasePrice) * orderItem.quantity
                itemProfit * orderItem.quantity
            }
        }

    /**
     * @param orders The list of orders to calculate the total profit from.
     * @param expenses The list of expenses to subtract from the total profit.
     * @return The total profit generated from the specified list of orders after subtracting the expenses.
     */
    private fun calculateTotalProfit(orders: List<Order>, expenses: List<Expense>) =
        calculateOrderProfits(orders) - expenses.sumOf { it.amount }


    /**
     * @param datePeriod The time period to filter orders by.
     * @return Data analytics insights based on the specified time period.
     */
    suspend fun getAnalytics(datePeriod: DatePeriod = DatePeriod.max()): Analytics{
        val orders = getOrders(datePeriod)
        val expenses = getExpenses(datePeriod)
        return Analytics(
            totalProfit = calculateTotalProfit(orders, expenses),
            mostPopularItems = getMostPopularItems(orders),
            mostProfitableItems = getMostProfitableItems(orders),
            mostProfitableCustomers = getMostProfitableCustomers(orders),
            timePeriod = TimePeriodDto.fromTimePeriod(datePeriod)
        )
    }

    /**
     * @param datePeriod The time period to filter orders by.
     * @return The total profit generated from the specified time period.
     */
    suspend fun getTotalProfit(datePeriod: DatePeriod = DatePeriod.max()): Int {
        val orders = getOrders(datePeriod)
        val expenses = getExpenses(datePeriod)
        return calculateTotalProfit(orders, expenses)
    }


    /**
     * @param datePeriod The time period to filter orders by.
     * @return The most popular item from the specified time period.
     */
    suspend fun getMostPopularItem(datePeriod: DatePeriod = DatePeriod.max()): MostPopularItemDto? {
        val orders = getOrders(datePeriod)
        return getMostPopularItem(orders)
    }

    /**
     * @param datePeriod The time period to filter orders by.
     * @return The most profitable item from the specified time period.
     */
    suspend fun getMostProfitableItem(datePeriod: DatePeriod = DatePeriod.max()): MostProfitableItemDto? {
        val orders = getOrders(datePeriod)
        return getMostProfitableItem(orders)
    }

    /**
     * @param datePeriod The time period to filter orders by.
     * @return The most profitable customer from the specified time period.
     */
    suspend fun getMostProfitableCustomer(datePeriod: DatePeriod): MostProfitableCustomerDto? {
        val orders = getOrders(datePeriod)
        return getMostProfitableCustomer(orders)
    }

    suspend fun getTopCustomers(datePeriod: DatePeriod = DatePeriod.max(), limit: Int = 10) =
        getMostProfitableCustomers(getOrders(datePeriod), limit)
}