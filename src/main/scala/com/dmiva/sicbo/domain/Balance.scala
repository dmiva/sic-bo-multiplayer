package com.dmiva.sicbo.domain

final case class Balance(amount: BigDecimal) extends AnyVal {
  def +(other: Balance): Balance = {
    copy(amount = amount + other.amount)
  }

}
