//go:build !race

package smartvectors

import (
	"fmt"
	"math/big"
	"testing"

	"github.com/consensys/zkevm-monorepo/prover/maths/fft"
	"github.com/consensys/zkevm-monorepo/prover/maths/field"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestFFTFuzzyDIFDIT(t *testing.T) {

	for i := 0; i < fuzzIteration; i++ {
		// We reuse the test case generator for linear combinations. We only
		// care about the first vector.
		builder := newTestBuilder(i)
		tcase := builder.NewTestCaseForLinComb()

		success := t.Run(
			fmt.Sprintf("fuzzy-FFT-DIT-DIF-%v", i),
			func(t *testing.T) {
				v := tcase.svecs[0]

				// Test the consistency of the FFT
				oncoset := builder.gen.Intn(2) == 0

				ratio, cosetID := 0, 0
				if oncoset {
					ratio = 1 << builder.gen.Intn(4)
					cosetID = builder.gen.Intn(ratio)
				}

				t.Logf("Parameters are (vec %v - ratio %v - cosetID %v", v.Pretty(), ratio, cosetID)

				// ====== Without bitreverse ======

				// FFT DIF and IFFT DIT should be the identity
				actual := FFT(v, fft.DIF, false, ratio, cosetID, nil)
				actual = FFTInverse(actual, fft.DIT, false, ratio, cosetID, nil)

				xA, xV := actual.Get(0), v.Get(0)
				assert.Equal(t, xA.String(), xV.String())
			},
		)

		require.True(t, success)
	}
}

func TestFFTFuzzyDITDIF(t *testing.T) {

	for i := 0; i < fuzzIteration; i++ {
		// We reuse the test case generator for linear combinations. We only
		// care about the first vector.
		builder := newTestBuilder(i)
		tcase := builder.NewTestCaseForLinComb()

		success := t.Run(
			fmt.Sprintf("fuzzy-FFT-DIT-DIF-%v", i),
			func(t *testing.T) {
				v := tcase.svecs[0]

				// Test the consistency of the FFT
				oncoset := builder.gen.Intn(2) == 0

				ratio, cosetID := 0, 0
				if oncoset {
					ratio = 1 << builder.gen.Intn(4)
					cosetID = builder.gen.Intn(ratio)
				}

				t.Logf("Parameters are (vec %v - ratio %v - cosetID %v", v.Pretty(), ratio, cosetID)

				// ====== Without bitreverse ======

				// FFT DIT and IFFT DIF should be the identity
				actual := FFT(v, fft.DIT, false, ratio, cosetID, nil)
				actual = FFTInverse(actual, fft.DIF, false, ratio, cosetID, nil)

				xA, xV := actual.Get(0), v.Get(0)
				assert.Equal(t, xA.String(), xV.String())
			},
		)

		require.True(t, success)
	}
}

func TestFFTFuzzyDIFDITBitReverse(t *testing.T) {

	for i := 0; i < fuzzIteration; i++ {
		// We reuse the test case generator for linear combinations. We only
		// care about the first vector.
		builder := newTestBuilder(i)
		tcase := builder.NewTestCaseForLinComb()

		success := t.Run(
			fmt.Sprintf("fuzzy-FFT-DIT-DIF-%v", i),
			func(t *testing.T) {
				v := tcase.svecs[0]

				// Test the consistency of the FFT
				oncoset := builder.gen.Intn(2) == 0

				ratio, cosetID := 0, 0
				if oncoset {
					ratio = 1 << builder.gen.Intn(4)
					cosetID = builder.gen.Intn(ratio)
				}

				t.Logf("Parameters are (vec %v - ratio %v - cosetID %v", v.Pretty(), ratio, cosetID)

				// ====== With bit reverse ======

				// FFT DIF and IFFT DIT should be the identity
				actual := FFT(v, fft.DIF, true, ratio, cosetID, nil)
				actual = FFTInverse(actual, fft.DIT, true, ratio, cosetID, nil)

				xA, xV := actual.Get(0), v.Get(0)
				assert.Equal(t, xA.String(), xV.String())
			},
		)

		require.True(t, success)
	}
}

func TestFFTFuzzyDITDIFBitReverse(t *testing.T) {

	for i := 0; i < fuzzIteration; i++ {
		// We reuse the test case generator for linear combinations. We only
		// care about the first vector.
		builder := newTestBuilder(i)
		tcase := builder.NewTestCaseForLinComb()

		success := t.Run(
			fmt.Sprintf("fuzzy-FFT-DIT-DIF-%v", i),
			func(t *testing.T) {
				v := tcase.svecs[0]

				// Test the consistency of the FFT
				oncoset := builder.gen.Intn(2) == 0

				ratio, cosetID := 0, 0
				if oncoset {
					ratio = 1 << builder.gen.Intn(4)
					cosetID = builder.gen.Intn(ratio)
				}

				t.Logf("Parameters are (vec %v - ratio %v - cosetID %v", v.Pretty(), ratio, cosetID)

				// ====== With bit reverse ======

				// FFT DIT and IFFT DIF should be the identity
				actual := FFT(v, fft.DIT, true, ratio, cosetID, nil)
				actual = FFTInverse(actual, fft.DIF, true, ratio, cosetID, nil)

				xA, xV := actual.Get(0), v.Get(0)
				assert.Equal(t, xA.String(), xV.String())
			},
		)

		require.True(t, success)
	}
}

func TestFFTFuzzyEvaluation(t *testing.T) {

	for i := 0; i < fuzzIteration; i++ {
		// We reuse the test case generator for linear combinations. We only
		// care about the first vector.
		builder := newTestBuilder(i)
		tcase := builder.NewTestCaseForLinComb()

		success := t.Run(
			fmt.Sprintf("fuzzy-FFT-DIT-DIF-%v", i),
			func(t *testing.T) {
				coeffs := tcase.svecs[0]

				// Test the consistency of the FFT
				oncoset := builder.gen.Intn(2) == 0

				ratio, cosetID := 0, 0
				if oncoset {
					ratio = 1 << builder.gen.Intn(4)
					cosetID = builder.gen.Intn(ratio)
				}

				// ====== With bit reverse ======

				// FFT DIT and IFFT DIF should be the identity
				evals := FFT(coeffs, fft.DIT, true, ratio, cosetID, nil)
				i := builder.gen.Intn(coeffs.Len())
				t.Logf("Parameters are (vec %v - ratio %v - cosetID %v - evalAt %v", coeffs.Pretty(), ratio, cosetID, i)

				x := fft.GetOmega(evals.Len())
				x.Exp(x, big.NewInt(int64(i)))

				if oncoset {
					omegacoset := fft.GetOmega(evals.Len() * ratio)
					omegacoset.Exp(omegacoset, big.NewInt(int64(cosetID)))
					mulGen := field.NewElement(field.MultiplicativeGen)
					omegacoset.Mul(&omegacoset, &mulGen)
					x.Mul(&omegacoset, &x)
				}

				yCoeff := EvalCoeff(coeffs, x)
				yFFT := evals.Get(i)

				require.Equal(t, yCoeff.String(), yFFT.String(), "evaluations are %v\n", evals.Pretty())

			},
		)

		require.True(t, success)
	}
}

func TestFFTFuzzyConsistWithInterpolation(t *testing.T) {

	for i := 0; i < fuzzIteration; i++ {
		// We reuse the test case generator for linear combinations. We only
		// care about the first vector.
		builder := newTestBuilder(i)
		tcase := builder.NewTestCaseForLinComb()

		success := t.Run(
			fmt.Sprintf("fuzzy-FFT-DIT-DIF-%v", i),
			func(t *testing.T) {
				coeffs := tcase.svecs[0]

				// Test the consistency of the FFT
				oncoset := builder.gen.Intn(2) == 0

				ratio, cosetID := 0, 0
				if oncoset {
					ratio = 1 << builder.gen.Intn(4)
					cosetID = builder.gen.Intn(ratio)
				}

				// ====== With bit reverse ======

				// FFT DIT and IFFT DIF should be the identity
				evals := FFT(coeffs, fft.DIT, true, ratio, cosetID, nil)
				i := builder.gen.Intn(coeffs.Len())
				t.Logf("Parameters are (vec %v - ratio %v - cosetID %v - evalAt %v", coeffs.Pretty(), ratio, cosetID, i)

				var xCoeff field.Element
				xCoeff.SetInt64(2)

				xVal := xCoeff

				if oncoset {
					omegacoset := fft.GetOmega(evals.Len() * ratio)
					omegacoset.Exp(omegacoset, big.NewInt(int64(cosetID)))
					mulGen := field.NewElement(field.MultiplicativeGen)
					omegacoset.Mul(&omegacoset, &mulGen)
					xVal.Div(&xVal, &omegacoset)
				}

				yCoeff := EvalCoeff(coeffs, xCoeff)
				// We already multiplied xVal by the multiplicative generator in the
				// important case.
				yFFT := Interpolate(evals, xVal, false)

				require.Equal(t, yCoeff.String(), yFFT.String())

			},
		)

		require.True(t, success)
	}
}

func TestFFTBackAndForth(t *testing.T) {

	// This test case is not covered from the above
	v := NewConstant(field.NewFromString("18761351033005093047639776353077664361612883771785172294598460731350692996243"), 1<<18)

	vcoeff := FFTInverse(v, fft.DIF, false, 0, 0, nil)
	vreeval0 := FFT(vcoeff, fft.DIT, false, 2, 0, nil)
	vreeval1 := FFT(vcoeff, fft.DIT, false, 2, 1, nil)

	require.Equal(t, v.Pretty(), vreeval0.Pretty())
	require.Equal(t, v.Pretty(), vreeval1.Pretty())

}
