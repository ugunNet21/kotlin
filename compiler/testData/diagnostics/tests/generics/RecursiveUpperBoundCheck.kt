open class C<T : C<T>>
class TestOK : C<TestOK>()
class TestFail : <!TYPE_MISMATCH!>C<<!UPPER_BOUND_VIOLATED!>C<<!UPPER_BOUND_VIOLATED!>TestFail<!>><!>><!>()
