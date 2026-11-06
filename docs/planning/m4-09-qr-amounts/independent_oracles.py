#!/usr/bin/env python3
"""Independent non-negative rational arithmetic, no application imports or binary floats.
Rule source: approved per-line HALF_UP cents, gross then discount then tax.
This is a technical test oracle, not legal homologation of a rate/regime.
"""
from fractions import Fraction as F
from pathlib import Path
import json,sys
CASES = [
 ("M409-01", "multiple-lines", [("1","10","VALOR","0"),("2","7.5","VALOR","0")]),
 ("M409-02", "fractional-six-decimals", [("1.234567","12.345678","VALOR","0"),("2.5","0.335","VALOR","0")]),
 ("M409-03", "percentage-discounts", [("2.75","3.333333","PERCENTAGEM","12.5"),("1.2","10.005","PERCENTAGEM","5.555555")]),
 ("M409-04", "absolute-discount-half-cent", [("1","0.335","VALOR","0.005"),("0.333333","9.999999","VALOR","1.005")]),
 ("M409-05", "line-versus-document-rounding", [("1","0.025","VALOR","0")]*3),
 ("M409-06", "percentage-on-closed-gross-discriminator", [("1","1.005","PERCENTAGEM","50")]),
]
def cents(value):
    assert value >= 0
    n,d=value.numerator*100,value.denominator
    return (2*n+d)//(2*d)
def money(c):return f"{c//100}.{c%100:02d}"
def generate():
    cases=[]
    for code,description,inputs in CASES:
        lines=[]
        for q,p,kind,discount in inputs:
            raw=F(q)*F(p); gross=cents(raw)
            raw_discount=F(gross,100)*F(discount)/100 if kind=="PERCENTAGEM" else F(discount)
            disc=cents(raw_discount); base=gross-disc; raw_tax=F(base,100)*F(23,100); tax=cents(raw_tax)
            assert base>=0
            lines.append(dict(input=dict(quantidade=q,precoUnitario=p,tipoDesconto=kind,desconto=discount),
                expected=dict(gross=money(gross),discount=money(disc),base=money(base),tax=money(tax),total=money(base+tax)),
                calculation=dict(rawGross=str(raw),closedGross=money(gross),rawDiscount=str(raw_discount),closedDiscount=money(disc),rawTax=str(raw_tax),closedTax=money(tax))))
        keys=["gross","discount","base","tax","total"]
        totals={k:money(sum(cents(F(line['expected'][k])) for line in lines)) for k in keys}
        cases.append(dict(id=code,description=description,rate="23",lines=lines,totals=totals))
    assert cases[0]['totals']==dict(gross="25.00",discount="0.00",base="25.00",tax="5.75",total="30.75")
    assert cases[4]['totals']==dict(gross="0.09",discount="0.00",base="0.09",tax="0.03",total="0.12")
    cases[4]['discriminators']={"sumRawGrossThenRound":"0.08","taxOnDocumentBaseThenRound":"0.02"}
    assert cases[5]['totals']==dict(gross="1.01",discount="0.51",base="0.50",tax="0.12",total="0.62")
    q,p,kind,d=CASES[5][2][0]
    wrong_discount=cents(F(q)*F(p)*F(d)/100)
    wrong_base=cents(F(q)*F(p))-wrong_discount
    wrong_tax=cents(F(wrong_base,100)*F(23,100))
    cases[5]['discriminators']=dict(discountOnUnclosedGross=money(wrong_discount),
        baseOnWrongDiscount=money(wrong_base),taxOnWrongBase=money(wrong_tax),totalOnWrongDiscount=money(wrong_base+wrong_tax))
    assert money(wrong_discount)=="0.50" and money(wrong_base+wrong_tax)=="0.63"
    return dict(rule="HALF_UP non-negative cents per line: close gross, discount, base, tax; sum closed lines",cases=cases)
if __name__=="__main__":
    payload=json.dumps(generate(),indent=2)+"\n"
    path=Path(__file__).resolve().parents[3]/'src/test/resources/m409/expected.json'
    if '--check' in sys.argv:
        assert path.read_text()==payload, 'Committed oracle differs from independent calculation'
        print('Oracle reproducible: 6 cases; execution selection is recorded separately')
    else:path.write_text(payload)
