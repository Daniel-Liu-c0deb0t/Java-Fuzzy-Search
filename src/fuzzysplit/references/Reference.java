package fuzzysplit.references;

import javafuzzysearch.utils.StrView;

import fuzzysplit.utils.Variables;

public interface Reference{
    public StrView get(Variables vars);
}
